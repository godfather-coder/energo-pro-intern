package com.example.mssqll.controller.File;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import com.example.mssqll.service.ExcelService;
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
        if (file.isEmpty()) {
                return new ApiResponse<>(false,"Please select a file to upload",null);
        }
        String fileName = file.getOriginalFilename();
        List<ExtractionResponseDto> extractionResponseDtoList = excelService.readExcel(file);

        return new ApiResponse<>(true,"File uploaded successfully: "+fileName,extractionResponseDtoList);
    }
   @GetMapping("/extractions")
    public ApiResponse<PagedModel<Extraction>> getExtractions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return new ApiResponse<>(true,"Data fetched",excelService.getAllExtractions(page, size));
    }
}
