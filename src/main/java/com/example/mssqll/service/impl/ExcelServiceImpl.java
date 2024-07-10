package com.example.mssqll.service.impl;

import com.example.mssqll.dto.request.ExtractionRequestDto;
import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import com.example.mssqll.repository.ExtractionRepository;
import com.example.mssqll.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ExcelServiceImpl implements ExcelService {
    private final ExtractionRepository extractionRepository;

    @Override
    public List<ExtractionResponseDto> readExcel(MultipartFile file) {
        List<ExtractionRequestDto> extractionRequestDtoList = new ArrayList<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                System.out.println("Sheet: " + sheet.getSheetName());
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        continue;
                    }

                    StringBuilder rowString = new StringBuilder("Row " + row.getRowNum() + ": ");
                    String dateStr = "";
                    int totalAmount = 0;
                    String purpose = "";
                    String description = "";

                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        switch (cell.getColumnIndex()) {
                            case 0:
                                dateStr = cellValue;
                                break;
                            case 1:
                                totalAmount = Integer.parseInt(cellValue);
                                break;
                            case 2:
                                purpose = cellValue;
                                break;
                            case 3:
                                description = cellValue;
                                break;
                        }
                        rowString.append(cellValue).append("\t");
                    }
                    System.out.println(rowString);

                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    extractionRequestDtoList.add(ExtractionRequestDto.builder()
                            .date(date)
                            .totalAmount(totalAmount)
                            .purpose(purpose)
                            .description(description)
                            .build());
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<ExtractionResponseDto> extractionResponseDtoList = new ArrayList<>();
        for (ExtractionRequestDto requestDto : extractionRequestDtoList) {
            Extraction extraction = Extraction.builder()
                    .date(requestDto.getDate())
                    .totalAmount(requestDto.getTotalAmount())
                    .purpose(requestDto.getPurpose())
                    .description(requestDto.getDescription())
                    .build();
            Extraction savedExtraction = extractionRepository.save(extraction);
            extractionResponseDtoList.add(ExtractionResponseDto.builder()
                    .id(savedExtraction.getId())
                    .date(savedExtraction.getDate())
                    .totalAmount(savedExtraction.getTotalAmount())
                    .purpose(savedExtraction.getPurpose())
                    .description(savedExtraction.getDescription())
                    .build());
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
    public ExtractionResponseDto create(Extraction extraction) {
        Extraction ext = extractionRepository.save(extraction);
        return ExtractionResponseDto.builder()
                .id(ext.getId())
                .date(ext.getDate())
                .totalAmount(ext.getTotalAmount())
                .purpose(ext.getPurpose())
                .description(ext.getDescription())
                .build();
    }

    @Override
    public PagedModel<Extraction> getAllExtractions(int page, int size) {
        return new PagedModel<>(extractionRepository.findAll(PageRequest.of(page, size)));
    }
}
