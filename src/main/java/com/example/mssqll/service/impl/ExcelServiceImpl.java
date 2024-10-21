package com.example.mssqll.service.impl;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.*;
import com.example.mssqll.repository.ConnectionFeeRepository;
import com.example.mssqll.repository.ExtractionRepository;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ExcelService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@RequiredArgsConstructor
@Service
public class ExcelServiceImpl implements ExcelService {
    @Autowired
    private EntityManager entityManager;
    private final ExtractionRepository extractionRepository;
    private final ExtractionTaskRepository extractionTaskRepository;
    private final ConnectionFeeRepository connectionFeeRepository;

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    public List<ExtractionResponseDto> readExcel(MultipartFile file) {
        LocalDateTime today = LocalDateTime.now();
        ExtractionTask extTask = extractionTaskRepository.save(new ExtractionTask(today, file.getOriginalFilename(), FileStatus.GOOD));
        List<ExtractionResponseDto> extractionResponseDtoList = new ArrayList<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                boolean fileStatus = true;
                for (Row row : sheet) {
                    if (row.getRowNum() < 2) {
                        continue;
                    }


                    LocalDate date = null;
                    Double totalAmount = (double) 0.0;
                    String purpose = "";
                    String description = "";
                    String tax = "";

                    Status status = Status.GOOD;


                    for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
                        Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                        if (!isCellEmpty(cell)) {
                            String cellValue = getCellValueAsString(cell);

                            switch (cellNum) {
                                case 0:
                                    if (!cellValue.isEmpty()) {
                                        try {
                                            date = LocalDate.parse(cellValue, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                        } catch (Exception e) {
                                            System.out.println(cellValue);
                                            continue;
                                        }
                                    }
                                    break;
                                case 1:
                                    if (!cellValue.isEmpty()) {
                                        try {
                                            totalAmount = Double.parseDouble(cellValue);
                                        } catch (NumberFormatException e) {
                                            totalAmount = 0.0;
                                        }
                                    }
                                    break;
                                case 2:
                                    purpose = cellValue;
                                    break;
                                case 3:
                                    description = cellValue;
                                    break;
                                case 4:
                                    tax = cellValue;
                            }
                        }
                    }

                    if (date == null && totalAmount == 0.0 && purpose.isEmpty() && description.isEmpty()) {
                        continue;
                    }

                    if (date == null || totalAmount == 0 || purpose.isEmpty() || description.isEmpty()) {
                        status = Status.WARNING;
                        fileStatus = false;
                    }

                    Extraction extraction = new Extraction();
                    extraction.setDate(date);
                    extraction.setTotalAmount(totalAmount);
                    extraction.setPurpose(purpose);
                    extraction.setDescription(description);
                    extraction.setStatus(status);
                    extraction.setTax(tax);
                    extraction.setExtractionTask(extTask);

                    if (!fileStatus) {
                        extTask.setStatus(FileStatus.WARNING);
                    } else {
                        extTask.setStatus(FileStatus.GOOD);
                    }
                    extractionTaskRepository.save(extTask);


                    Extraction excelExtraction = extractionRepository.save(extraction);
                    extractionResponseDtoList.add(ExtractionResponseDto.builder()
                            .id(excelExtraction.getId())
                            .date(excelExtraction.getDate())
                            .totalAmount(excelExtraction.getTotalAmount())
                            .purpose(excelExtraction.getPurpose())
                            .description(excelExtraction.getDescription())
                            .status(excelExtraction.getStatus())
                            .extractionTask(excelExtraction.getExtractionTask())
                            .build());
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
        return new PagedModel<>(extractionRepository.findByStatus(Status.WARNING, PageRequest.of(page, size)));
    }

    @Override
    public PagedModel<Extraction> getAllOkExtractions(int page, int size) {
        return new PagedModel<>(extractionRepository.findByStatus(Status.GOOD, PageRequest.of(page, size)));
    }

    @Override
    public PagedModel<Extraction> getAllExtractionsWithFile(int page, int size, Long fileId) {
        return new PagedModel<>(extractionRepository.findByExtractionTaskId(fileId, PageRequest.of(page, size)));
    }

    @Override
    public PagedModel<Extraction> getAllWarningExtractionsWithFile(int page, int size, Long fileid) {
        return null;
    }

    @Override
    public PagedModel<Extraction> getAllOkExtractionsWithFile(int page, int size, Long fileid) {
        return new PagedModel<>(extractionRepository.findByExtractionTaskIdAndStatus(fileid, Status.GOOD, PageRequest.of(page, size)));
    }

    @Override
    public Long getGrandTotal(ExtractionTask taskId) {
        return extractionRepository.sumTotalAmountByExtractionTask(taskId);
    }

    @Override
    public Long getTotalWarning() {
        return extractionRepository.countAllByStatus(Status.WARNING);
    }

    @Override
    public Long getTotalOk() {
        return extractionRepository.countAllByStatus(Status.GOOD);
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
    public Long sumTotalAmountByStatus(Status status) {
        return extractionRepository.sumTotalAmountByStatus(Status.GOOD);
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
        return new PagedModel<>(extractionRepository.findByExtractionTaskIdAndStatus(extractionTaskId, Status.WARNING, PageRequest.of(page, size))
        );
    }

    @Override
    public void deleteById(Long id) {
        extractionRepository.deleteByExtractionTask(extractionTaskRepository.getReferenceById(id));
        connectionFeeRepository.deleteByExtractionTaskId(id);
        extractionTaskRepository.deleteById(id);
    }

    @Override
    public PagedModel<Extraction> getByDate(LocalDate from, LocalDate to, int page, int size) {
        return new PagedModel<>(extractionRepository.findAllByDateBetween(from, to, PageRequest.of(page, size)));
    }

    @Override
    public Long getWarnCountByDate(LocalDate from, LocalDate to) {
        return extractionRepository.countWarningsByDate(from, to);
    }

    @Override
    public Long getOkCountByDate(LocalDate from, LocalDate to) {
        return extractionRepository.countOksByDate(from, to);
    }

    @Override
    public Long sumByDate(LocalDate from, LocalDate to) {
        return extractionRepository.sumByDate(from, to);
    }

    @Override
    public PagedModel<Extraction> getByTotalAmount(Long totalAmount, int page, int size) {
        return new PagedModel<>(extractionRepository.findByTotalAmount(totalAmount, PageRequest.of(page, size)));
    }

    @Override
    public Long countByTotalAmountAndStatus(Long totalAmount, Status status) {
        return extractionRepository.countByTotalAmountAndStatus(totalAmount, status);
    }

    @Override
    public Long sumByTotalAmount(Long totalAmount) {
        return extractionRepository.sumByTotalAmount(totalAmount);
    }

    @Override
    public PagedModel<Extraction> getByAmountAndStatus(Long total, int page, int size, Status status) {
        return new PagedModel<>(extractionRepository.findByTotalAmountAndStatus(total, PageRequest.of(page, size), status));
    }

    @Override
    public Map<String, Object> letsDoExtractionFilter(Specification<Extraction> spec, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);

        // Fetch paginated results for the given Specification
        PagedModel<Extraction> excPage = new PagedModel<>(extractionRepository.findAll(spec, PageRequest.of(page, size, sort)));

        // Calculate the sum of totalAmount using CriteriaBuilder
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> sumQuery = cb.createQuery(Double.class);
        Root<Extraction> root = sumQuery.from(Extraction.class);
        sumQuery.select(cb.sum(root.get("totalAmount")));

        // Apply Specification as Predicate (if exists)
        Predicate predicate = spec.toPredicate(root, sumQuery, cb);
        if (predicate != null) {
            sumQuery.where(predicate);
        }

        // Execute the sum query
        Double totalAmountSum = entityManager.createQuery(sumQuery).getSingleResult();

        // Prepare the response map
        Map<String, Object> response = new HashMap<>();
        response.put("totalAmountSum", totalAmountSum);
        response.put("excPage", excPage);

        return response;
    }

    @Override
    public ExtractionRepository getRepo() {
        return this.extractionRepository;
    }
}
