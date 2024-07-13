package com.example.mssqll.controller.File;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import com.example.mssqll.service.ExcelService;
import com.example.mssqll.utiles.exceptions.FileNotSupportedException;
import com.example.mssqll.utiles.exceptions.ResourceNotFoundException;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/excels")
public class ExcelController {
    @Autowired
    private ExcelService excelService;

    @PostMapping("/upload")
    public ApiResponse<List<ExtractionResponseDto>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        System.out.println("okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Please select a file to upload");
        }
        String contentType = file.getContentType();
        if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) &&
            !"application/vnd.ms-excel".equals(contentType)) {
            throw new FileNotSupportedException("Only Excel files are supported");
        }
        System.out.println("okkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
        String fileName = file.getOriginalFilename();
        List<ExtractionResponseDto> extractionResponseDtoList = excelService.readExcel(file);
        System.out.println("okkkkkkkkkkkkkkkkkkkkkkkkkkk2");
        return new ApiResponse<>(true, "File uploaded successfully: " + fileName, extractionResponseDtoList);
    }

    @GetMapping("/extractions")
    public ApiResponse<PagedModel<Extraction>> getExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        return new ApiResponse<>(true, "Data fetched", excelService.getAllExtractions(adjustedPage, size));
    }

    @GetMapping("/extractions/warning")
    public ApiResponse<PagedModel<Extraction>> getWarningExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        return new ApiResponse<>(true, "Warned Data", excelService.getAllWarningExtractions(adjustedPage, size));
    }
    @GetMapping("/extractions/ok")
    public ApiResponse<PagedModel<Extraction>> getOkExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        return new ApiResponse<>(true, "Ok Data", excelService.getAllOkExtractions(adjustedPage, size));
    }


}
