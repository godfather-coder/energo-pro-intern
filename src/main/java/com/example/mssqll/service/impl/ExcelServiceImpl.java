package com.example.mssqll.service.impl;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.repository.ExtractionRepository;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ExcelServiceImpl implements ExcelService {
    private final ExtractionRepository extractionRepository;
    private final ExtractionTaskRepository extractionTaskRepository;

private boolean isCellEmpty(Cell cell) {
    return cell == null || cell.getCellType() == CellType.BLANK;
}
    @Override
    public List<ExtractionResponseDto> readExcel(MultipartFile file) {
        System.out.println("readExcel");
        LocalDateTime today = LocalDateTime.now();
        ExtractionTask extTask = extractionTaskRepository.save(new ExtractionTask(today, file.getOriginalFilename(), 1));
        List<ExtractionResponseDto> extractionResponseDtoList = new ArrayList<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                System.out.println("Sheet: " + sheet.getSheetName());
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        continue;
                    }
                    Extraction extraction = new Extraction();
                    LocalDate date = null;
                    int totalAmount = 0;
                    String purpose = "";
                    String description = "";
                    int status = 1;
                    boolean notbs = true;

                    for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
                        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                        if (isCellEmpty(cell)) {
                        } else {
                            String cellValue = getCellValueAsString(cell);

                            switch (cellNum) {
                                case 0:
                                    if (!cellValue.isEmpty()) {
                                        date = LocalDate.parse(cellValue, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                    }
                                    break;
                                case 1:
                                    if (!cellValue.isEmpty()) {
                                        try {
                                            totalAmount = Integer.parseInt(cellValue);
                                        } catch (NumberFormatException e) {
                                            totalAmount = 0;
                                        }
                                    }
                                    break;
                                case 2:
                                    purpose = cellValue;
                                    break;
                                case 3:
                                    description = cellValue;
                                    break;
                            }
                        }
                    }

                    if (date == null || totalAmount == 0) {
                        status = 0;
                    }
                    if(date == null && totalAmount == 0 && purpose == "" && description == ""){
                        status = 1;
                    }

                    extraction.setDate(date);
                    extraction.setTotalAmount(totalAmount);
                    extraction.setPurpose(purpose);
                    extraction.setDescription(description);
                    extraction.setStatus(status);
                    extraction.setExtractionTask(extTask);

                    if(status == 0) {
                        extTask.setStatus(0);
                        extractionTaskRepository.save(extTask);
                    }
                     if(date == null && totalAmount == 0 && purpose == "" && description == ""){
                         System.out.println(extraction);
                         continue;
                     }
                    Extraction excelExtraction = extractionRepository.save(extraction);
                    extractionResponseDtoList.add(ExtractionResponseDto.builder()
                            .id(excelExtraction.getId())
                            .date(excelExtraction.getDate())
                            .totalAmount(excelExtraction.getTotalAmount())
                            .purpose(excelExtraction.getPurpose())
                            .description(excelExtraction.getDescription())
                            .status(excelExtraction.getStatus())
                            .extractionTask(excelExtraction.getExtractionTask())
                            .build()
                    );
                }
            }
    } catch (IOException e) {
        e.printStackTrace();
    }
         return extractionResponseDtoList;
    }

    public String getCellValueAsString(Cell cell) {
       if (cell == null) {
        return "";
    }

    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue();
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return new SimpleDateFormat("dd/MM/yyyy").format(date);
            } else {
                return String.valueOf((int) cell.getNumericCellValue());
            }
        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        case FORMULA:
            return cell.getCellFormula();
        case BLANK:
            return "";
        default:
            return "Unknown Cell Type";
    }
    }


    @Override
    public PagedModel<Extraction> getAllExtractions(int page, int size) {
        return new PagedModel<>(extractionRepository.findAll(PageRequest.of(page, size)));
    }

    @Override
    public PagedModel<Extraction> getAllWarningExtractions(int page, int size) {
        return new PagedModel<>(extractionRepository.findByStatus(0,PageRequest.of(page, size)));
    }
    @Override
    public PagedModel<Extraction> getAllOkExtractions(int page, int size) {
        return new PagedModel<>(extractionRepository.findByStatus(1,PageRequest.of(page, size)));
    }
    @Override
    public PagedModel<Extraction> getAllExtractionsWithFile(int page, int size, Long fileId){
        return new PagedModel<>(extractionRepository.findByExtractionTaskId(fileId,PageRequest.of(page,size)));
    }

    @Override
    public PagedModel<Extraction> getAllWarningExtractionsWithFile(int page, int size, Long fileid) {
        return null;
    }

    @Override
    public PagedModel<Extraction> getAllOkExtractionsWithFile(int page, int size,Long fileid) {
        return new PagedModel<>(extractionRepository.findByExtractionTaskIdAndStatus(fileid,1,PageRequest.of(page, size)));
    }

    @Override
    public Long getGrandTotal(ExtractionTask taskId) {
        return extractionRepository.sumTotalAmountByExtractionTask(taskId);
    }

    @Override
    public Long getTotalWarning() {
        return extractionRepository.countAllByStatus(0);
    }

    @Override
    public Long getTotalOk() {
        return extractionRepository.countAllByStatus(1);
    }

    @Override
    public Long getTotalExtractionCount() {
        return extractionRepository.count();
    }

    @Override
    public Long getGrandTotal() {
        return extractionRepository.sumTotalAmount();
    }


    @Override
    public Long getExtractionCountByExtractionTaskId(ExtractionTask extractionTask) {
        return extractionRepository.countAllByExtractionTaskId(extractionTask.getId());
    }

    @Override
    public Long sumTotalAmountByStatus(int status) {
        return extractionRepository.sumTotalAmountByStatus(status);
    }

    @Override
    public Long getTotalAmountByExtractionTaskId(Long id) {
        return extractionRepository.sumTotalAmountByExtractionTask(
                extractionTaskRepository.getReferenceById(id)
        );
    }

    @Override
    public Long getCountWarningsByFileId(Long fileId) {
        return extractionRepository.countWarningsByFileId(extractionTaskRepository.getReferenceById(fileId));

    }

    @Override
    public Long getCountOkByFileId(Long fileId) {
         return extractionRepository.countOkByFileId(extractionTaskRepository.getReferenceById(fileId));
    }

    @Override
    public PagedModel<Extraction> getWarningByExtractionTask(Long extractionTaskId, int page, int size) {
        return new PagedModel<>(extractionRepository.findByExtractionTaskIdAndStatus(extractionTaskId,0,PageRequest.of(page, size))
        );
    }

    @Override
    public void deleteById(Long id) {
        extractionRepository.deleteByExtractionTask(extractionTaskRepository.getReferenceById(id));
        extractionTaskRepository.deleteById(id);
    }

    @Override
    public PagedModel<Extraction> getByDate(LocalDate from, LocalDate to,int page, int size) {
        return new PagedModel<>(extractionRepository.findAllByDateBetween(from,to,PageRequest.of(page,size)));
    }

    @Override
    public Long getWarnCountByDate(LocalDate from, LocalDate to) {
        return extractionRepository.countWarningsByDate(from,to);
    }

    @Override
    public Long getOkCountByDate(LocalDate from, LocalDate to) {
       return extractionRepository.countOksByDate(from,to);
    }

    @Override
    public Long sumByDate(LocalDate from, LocalDate to) {
        return extractionRepository.sumByDate(from,to);
    }

    @Override
    public PagedModel<Extraction> getByTotalAmount(Long totalAmount, int page, int size) {
        return new PagedModel<>(extractionRepository.findByTotalAmount(totalAmount,PageRequest.of(page,size)));
    }

    @Override
    public Long countByTotalAmountAndStatus(Long totalAmount, int status) {
        return extractionRepository.countByTotalAmountAndStatus(totalAmount,status);
    }

    @Override
    public Long sumByTotalAmount(Long totalAmount) {
        return extractionRepository.sumByTotalAmount(totalAmount);
    }

    @Override
    public PagedModel<Extraction> getByAmountAndStatus(Long total,int page, int size, int status) {
        return new PagedModel<>(extractionRepository.findByTotalAmountAndStatus(total, PageRequest.of(page,size),status));
    }

    @Override
    public ExtractionRepository getRepo() {
        return this.extractionRepository;
    }
}
