package com.example.mssqll.service;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import org.springframework.data.web.PagedModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ExcelService{
     List<ExtractionResponseDto> readExcel(MultipartFile file);
     ExtractionResponseDto create(Extraction extraction);
     PagedModel<Extraction> getAllExtractions(int page, int size) ;
     PagedModel<Extraction> getAllWarningExtractions(int page, int size) ;
     PagedModel<Extraction> getAllOkExtractions(int page, int size) ;
}
