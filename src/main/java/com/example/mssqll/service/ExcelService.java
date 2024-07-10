package com.example.mssqll.service;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import org.springframework.data.web.PagedModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ExcelService{
    public List<ExtractionResponseDto> readExcel(MultipartFile file);
    public ExtractionResponseDto create(Extraction extraction);
    public PagedModel<Extraction> getAllExtractions(int page, int size) ;
}
