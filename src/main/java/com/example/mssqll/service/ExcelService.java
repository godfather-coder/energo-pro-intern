package com.example.mssqll.service;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.repository.ExtractionRepository;
import org.springframework.data.web.PagedModel;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;


public interface ExcelService{
     ExtractionRepository getRepo();
     List<ExtractionResponseDto> readExcel(MultipartFile file);
     PagedModel<Extraction> getAllExtractions(int page, int size) ;
     PagedModel<Extraction> getAllWarningExtractions(int page, int size) ;
     PagedModel<Extraction> getAllOkExtractions(int page, int size) ;
     PagedModel<Extraction> getAllOkExtractionsWithFile(int page, int size,Long fileid) ;
     PagedModel<Extraction> getAllExtractionsWithFile(int page, int size, Long fileid);
     PagedModel<Extraction> getAllWarningExtractionsWithFile(int page, int size, Long fileid);
     Long getGrandTotal(ExtractionTask extractionTask);
     Long getTotalWarning();
     Long getTotalOk();
     Long getTotalExtractionCount();
     Long getGrandTotal();
     Long getExtractionCountByExtractionTaskId(ExtractionTask extractionTask);
     Long sumTotalAmountByStatus(int status);
     Long getTotalAmountByExtractionTaskId(Long id);
     Long getCountWarningsByFileId(Long fileId) ;
     Long getCountOkByFileId(Long fileId) ;
     PagedModel<Extraction> getWarningByExtractionTask(Long extractionTaskId, int page, int size) ;
     void deleteById(Long id);
     PagedModel<Extraction> getByDate(LocalDate from, LocalDate to, int page, int size);
     Long getWarnCountByDate(LocalDate from, LocalDate to);
     Long getOkCountByDate(LocalDate from, LocalDate to);
     Long sumByDate(LocalDate from, LocalDate to);
     PagedModel<Extraction> getByTotalAmount(Long totalAmount, int page, int size);
     Long countByTotalAmountAndStatus(Long totalAmount, int status);
     Long sumByTotalAmount(Long totalAmount);
     PagedModel<Extraction> getByAmountAndStatus(Long total,int page, int size, int status);
}
