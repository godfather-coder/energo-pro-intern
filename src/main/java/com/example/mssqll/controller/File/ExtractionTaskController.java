package com.example.mssqll.controller.File;


import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.service.impl.ExtractionTaskServiceImpl;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/extraction-task")
public class ExtractionTaskController {
    @Autowired
    ExtractionTaskServiceImpl extractionTaskService;

    @GetMapping("/all-upls")
    public ApiResponse<PagedModel<ExtractionTask>> getAllUpls(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        return new ApiResponse<>(true, "Data fetched",extractionTaskService.getExtractionTasks(adjustedPage,size));
    }

}
