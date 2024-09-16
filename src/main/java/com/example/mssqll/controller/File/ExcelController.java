package com.example.mssqll.controller.File;

import com.example.mssqll.dto.response.ExtractionResponseDto;
import com.example.mssqll.models.Extraction;
import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.models.Status;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ExcelService;
import com.example.mssqll.utiles.exceptions.FileNotSupportedException;
import com.example.mssqll.utiles.exceptions.ResourceNotFoundException;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/excels")
public class ExcelController {
    @Autowired
    private ExcelService excelService;
    @Autowired
    private ExtractionTaskRepository extractionTaskRepository;

    @PostMapping("/upload")
    public ApiResponse<List<ExtractionResponseDto>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Please select a file to upload");
        }
        String contentType = file.getContentType();
        if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) &&
            !"application/vnd.ms-excel".equals(contentType)) {
            throw new FileNotSupportedException("Only Excel files are supported");
        }
        List<ExtractionResponseDto> extractionResponseDtoList = excelService.readExcel(file);
        Long warn = extractionResponseDtoList.stream().filter(extractionResponseDto -> extractionResponseDto.getStatus() == Status.WARNING).count();
        Long ok = extractionResponseDtoList.size() - warn;
        Long gt = excelService.getGrandTotal(extractionResponseDtoList.get(0).getExtractionTask())+warn;
        ApiResponse<List<ExtractionResponseDto>> res = new ApiResponse<>(true,
                "Operation Successful",
                         extractionResponseDtoList,
                         warn,
                ok,
                gt,
                excelService.getExtractionCountByExtractionTaskId(extractionResponseDtoList.get(0).getExtractionTask())
                );
        return res;
    }

    @GetMapping("/extractions")
    public ApiResponse<PagedModel<Extraction>> getExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        Long ok = excelService.getTotalOk();
        Long warn = excelService.getTotalWarning();
        Long total = excelService.getTotalExtractionCount();
        PagedModel<Extraction> extractions = excelService.getAllExtractions(adjustedPage, size);
        Long totalAmount = excelService.getGrandTotal();
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Operation successful")
                .data(extractions)
                .warn(warn)
                .ok(ok)
                .grandTotal(total)
                .countAll(totalAmount)
                .build();
    }

    @GetMapping("/extractions/warning")
    public ApiResponse<PagedModel<Extraction>> getWarningExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        Long ok = excelService.getTotalOk();
        Long warn = excelService.getTotalWarning();
        PagedModel<Extraction> extractions = excelService.getAllWarningExtractions(adjustedPage, size);
        Long totalAmount = excelService.sumTotalAmountByStatus(Status.WARNING);
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Warned Data")
                .data(extractions)
                .warn(warn)
                .ok(ok)
                .grandTotal(totalAmount)
                .countAll(warn)
                .build();
    }
    @GetMapping("/extractions/ok")
    public ApiResponse<PagedModel<Extraction>> getOkExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        Long ok = excelService.getTotalOk();
        Long warn = excelService.getTotalWarning();
        PagedModel<Extraction> extractions = excelService.getAllOkExtractions(adjustedPage, size);
        Long totalAmount = excelService.sumTotalAmountByStatus(Status.GOOD);
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Ok Data")
                .data(extractions)
                .warn(warn)
                .ok(ok)
                .grandTotal(totalAmount)
                .countAll(ok)
                .build();
    }

    @GetMapping("/getExtractionsByFile")
    public ApiResponse<PagedModel<Extraction>> getExtractionByFile(
             @RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam  Long fileId){
        int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<Extraction> extractionPage = excelService.getAllExtractionsWithFile(adjustedPage, size, fileId);

        Long warn = excelService.getCountWarningsByFileId(fileId);
        Long ok = excelService.getCountOkByFileId(fileId);
        Long grandTotal = excelService.getTotalAmountByExtractionTaskId(fileId);
        Long total = excelService.getExtractionCountByExtractionTaskId(extractionTaskRepository.getReferenceById(fileId));
        ApiResponse<PagedModel<Extraction>> response = new ApiResponse<>(
                true,
                "Data retrieved successfully",
                extractionPage,
                warn,
                ok,
                grandTotal,
                total
        );

        return response;
    }

    @GetMapping("/getWarningExtractionsByFile")
    public ApiResponse<PagedModel<Extraction>> getWarningExtractionByFile(
             @RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam  Long fileId){
        int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<Extraction> extractionsPagedModel  = excelService.getWarningByExtractionTask(fileId,adjustedPage,size);
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Ok Data")
                .data(extractionsPagedModel)
                .warn(null)
                .ok(null)
                .grandTotal(excelService.getRepo().sumTotalAmountByExtractionTaskAndStatus(extractionTaskRepository.getReferenceById(fileId),Status.WARNING))
                .countAll(extractionsPagedModel.getMetadata().totalElements())
                .build();
    }

    @GetMapping("/getOkExtractionsByFile")
    public ApiResponse<PagedModel<Extraction>> getOkExtractionByFile(
             @RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam  Long fileId){
        int adjustedPage = (page < 1) ? 0 : page - 1;
         Long ok = excelService.getTotalOk();
        Long warn = excelService.getTotalWarning();
        PagedModel<Extraction> extractions =  excelService.getAllOkExtractionsWithFile(adjustedPage,size,fileId);
        Long totalAmount = excelService.getTotalAmountByExtractionTaskId(fileId);
        Long total = excelService.getExtractionCountByExtractionTaskId(extractionTaskRepository.getReferenceById(fileId));
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Ok Data")
                .data(extractions)
                .warn(warn)
                .ok(ok)
                .grandTotal(totalAmount)
                .countAll(extractions.getMetadata().totalElements())
                .build();
    }

    @DeleteMapping("/delete")
    public ApiResponse<ExtractionTask> deleteExtractionTask(@RequestParam Long taskId) {
        excelService.deleteById(taskId);
        return ApiResponse.<ExtractionTask>builder()
                .success(true)
                .message("File Deleted")
                .data(null)
                .warn(null)
                .ok(null)
                .grandTotal(null)
                .countAll(null)
                .build();
    }
    /// new task from davit 30.07.2024
    @GetMapping("/getByDate")
    public ApiResponse<PagedModel<Extraction>> getByDate(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<Extraction> extractionsPagedModel  = excelService.getByDate(startDate,endDate,adjustedPage,size);
        System.out.println(startDate);
        System.out.println(endDate);
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Ok Data")
                .data(extractionsPagedModel)
                .warn(excelService.getWarnCountByDate(startDate,endDate))
                .ok(excelService.getOkCountByDate(startDate,endDate))
                .grandTotal(excelService.sumByDate(startDate,endDate))
                .countAll(extractionsPagedModel.getMetadata().totalElements())
                .build();
    }

    @GetMapping("/getByAmount")
    public ApiResponse<PagedModel<Extraction>> getByPrice(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam("amount") Long amount) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<Extraction> extractionsPagedModel  = excelService.getByTotalAmount(amount,adjustedPage,size);
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Ok Data")
                .data(extractionsPagedModel)
                .warn(excelService.countByTotalAmountAndStatus(amount,Status.WARNING))
                .ok(excelService.countByTotalAmountAndStatus(amount,Status.GOOD))
                .grandTotal(excelService.sumByTotalAmount(amount))
                .countAll(extractionsPagedModel.getMetadata().totalElements())
                .build();

    }
    @GetMapping("/getByAmountAndStatus")
    public ApiResponse<PagedModel<Extraction>> getByAmountAndStatus(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam("amount") Long amount,
            @RequestParam("status") Status status){
        int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<Extraction> extractionsPagedModel  = excelService.getByAmountAndStatus(amount,adjustedPage,size,status);
        return ApiResponse.<PagedModel<Extraction>>builder()
                .success(true)
                .message("Ok Data")
                .data(extractionsPagedModel)
                .warn(excelService.countByTotalAmountAndStatus(amount,Status.WARNING))
                .ok(excelService.countByTotalAmountAndStatus(amount,Status.GOOD))
                .grandTotal(excelService.getRepo().sumByTotalAmountAndStatus(amount,status))
                .countAll(extractionsPagedModel.getMetadata().totalElements())
                .build();
    }

}
