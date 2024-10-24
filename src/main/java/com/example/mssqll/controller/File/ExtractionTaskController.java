package com.example.mssqll.controller.File;


import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.service.impl.ExtractionTaskServiceImpl;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/extraction-task")
public class ExtractionTaskController {
    @Autowired
    ExtractionTaskServiceImpl extractionTaskService;

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @GetMapping("/all-upls")
    public ApiResponse<PagedModel<ExtractionTask>> getAllUpls(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        return new ApiResponse<>(true, "Data fetched",extractionTaskService.getExtractionTasks(adjustedPage,size));
    }
    @GetMapping("/find-by-name/{fileName}")
    public ApiResponse<List<ExtractionTask>> findByName(@PathVariable String fileName) {
        List<ExtractionTask> extractionTasks = extractionTaskService.findByName(fileName);
        return new ApiResponse<>(true, "Files fetched", extractionTasks);
    }
}
