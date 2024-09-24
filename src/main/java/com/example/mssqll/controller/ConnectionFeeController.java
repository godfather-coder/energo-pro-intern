package com.example.mssqll.controller;


import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.service.ConnectionFeeService;
import com.example.mssqll.specifications.ConnectionFeeSpecification;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/connection-fees")
public class ConnectionFeeController {

    private final ConnectionFeeService connectionFeeService;

    @Autowired
    public ConnectionFeeController(ConnectionFeeService connectionFeeService) {
        this.connectionFeeService = connectionFeeService;
    }

    @GetMapping
    public ApiResponse<PagedModel<ConnectionFee>> getExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<ConnectionFee> fees = connectionFeeService.getAllFee(adjustedPage, size);
        return ApiResponse.<PagedModel<ConnectionFee>>builder()
                .success(true)
                .message("Operation successful")
                .data(fees)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ConnectionFee> getConnectionFee(@PathVariable Long id) {
        Optional<ConnectionFee> fee = connectionFeeService.getFee(id);
        if (fee.isPresent()) {
            return ApiResponse.<ConnectionFee>builder()
                    .success(true)
                    .message("Operation successful")
                    .data(fee.get())
                    .build();
        } else {
            return ApiResponse.<ConnectionFee>builder()
                    .success(false)
                    .message("Connection Fee not found")
                    .build();
        }
    }

    @PostMapping("/{extractionTaskId}")
    public ApiResponse<List<ConnectionFee>> createConnectionFee(@PathVariable Long extractionTaskId) {
        List<ConnectionFee> createdConnectionFee = connectionFeeService.saveFee(extractionTaskId);

        return ApiResponse.<List<ConnectionFee>>builder()
                .success(true)
                .message("Connection Fee created successfully")
                .data(createdConnectionFee)
                .build();
    }

    @PostMapping()
    public ApiResponse<ConnectionFee> createConnectionFee(@RequestBody ConnectionFee connectionFee) {
        ConnectionFee fee = connectionFeeService.save(connectionFee);
        return ApiResponse.<ConnectionFee>builder()
                .success(true)
                .message("Operation successful")
                .data(fee)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConnectionFee(@PathVariable Long id) {
        Optional<ConnectionFee> optionalConnectionFee = connectionFeeService.findById(id);

        if (optionalConnectionFee.isPresent()) {
            connectionFeeService.deleteById(id);

            return ApiResponse.<Void>builder()
                    .success(true)
                    .message("Connection Fee deleted successfully")
                    .build();
        } else {

            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("Connection Fee not found")
                    .build();
        }
    }

    @PutMapping("/{connectionFeeId}")
    public ApiResponse<ConnectionFee> updateConnectionFee(
            @PathVariable Long connectionFeeId,
            @RequestBody ConnectionFee connectionFeeDetails) {

        ConnectionFee updatedConnectionFee = connectionFeeService.updateFee(connectionFeeId, connectionFeeDetails);
        return ApiResponse.<ConnectionFee>builder()
                .success(true)
                .message("Connection Fee deleted successfully")
                .data(updatedConnectionFee)
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<PagedModel<ConnectionFee>> filterConnectionFees(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id")String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        Specification<ConnectionFee> spec = ConnectionFeeSpecification.getSpecifications((Map) filters);
        return ApiResponse.<PagedModel<ConnectionFee>>builder()
                .success(true)
                .message("Filtered data")
                .data(connectionFeeService.letDoFilter(spec,adjustedPage,size,sortBy,sortDir))
                .build();
    }
    @DeleteMapping("/delete-by-task/{extractionTaskId}")
     public ApiResponse<List<ConnectionFee>> deleteConnectionFeeByTaskId(@PathVariable Long extractionTaskId) {
        connectionFeeService.deleteByTaskId(extractionTaskId);
        return ApiResponse.<List<ConnectionFee>>builder()
                .success(true)
                .message("Connections Deleted Successfully")
                .build();
    }


}