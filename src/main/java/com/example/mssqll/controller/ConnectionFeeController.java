package com.example.mssqll.controller;


import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.service.ConnectionFeeService;
import com.example.mssqll.specifications.ConnectionFeeSpecification;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    public ResponseEntity<PagedModel<ConnectionFee>> getExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<ConnectionFee> fees = connectionFeeService.getAllFee(adjustedPage, size);
        return ResponseEntity.ok().body(fees);
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
    public ResponseEntity<List<ConnectionFee>> createConnectionFee(@PathVariable Long extractionTaskId) {
        List<ConnectionFee> createdConnectionFee = connectionFeeService.saveFee(extractionTaskId);
        return ResponseEntity.ok().body(createdConnectionFee);
    }

    @PostMapping()
    public ResponseEntity<ConnectionFee> createConnectionFee(@RequestBody ConnectionFee connectionFee) {
        ConnectionFee fee = connectionFeeService.save(connectionFee);
        return ResponseEntity.ok().body(fee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConnectionFee(@PathVariable Long id) {
        Optional<ConnectionFee> optionalConnectionFee = connectionFeeService.findById(id);
        if (optionalConnectionFee.isPresent()) {
            connectionFeeService.deleteById(id);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee deleted successfully"));
        } else {
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee not found"));
        }
    }

    @PutMapping("/{connectionFeeId}")
    public ResponseEntity<ConnectionFee> updateConnectionFee(
            @PathVariable Long connectionFeeId,
            @RequestBody ConnectionFee connectionFeeDetails) {

        ConnectionFee updatedConnectionFee = connectionFeeService.updateFee(connectionFeeId, connectionFeeDetails);
        return ResponseEntity.ok().body(updatedConnectionFee);
    }

    @GetMapping("/filter")
    public ResponseEntity<PagedModel<ConnectionFee>> filterConnectionFees(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        Specification<ConnectionFee> spec = ConnectionFeeSpecification.getSpecifications((Map) filters);
        return ResponseEntity.ok().body(connectionFeeService.letDoFilter(spec, adjustedPage, size, sortBy, sortDir));
    }

    @DeleteMapping("/delete-by-task/{extractionTaskId}")
    public ResponseEntity<?> deleteConnectionFeeByTaskId(@PathVariable Long extractionTaskId) {
        connectionFeeService.deleteByTaskId(extractionTaskId);
        return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee deleted successfully"));
    }

    @DeleteMapping("/soft-delete/{fee}")
    public ResponseEntity<?> softDeleteConnectionFee(@PathVariable Long fee) {
        Optional<ConnectionFee> optionalConnectionFee = connectionFeeService.findById(fee);
        if (optionalConnectionFee.isPresent()) {
            connectionFeeService.softDeleteById(fee);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee deleted successfully"));
        } else {
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee not found"));
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel() throws IOException {
        List<ConnectionFee> connectionFees = connectionFeeService.getAllConnectionFees();

        ByteArrayInputStream excelStream = connectionFeeService.createExcel(connectionFees);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=connection_fees.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelStream.readAllBytes());
    }

    @PostMapping("/divide-fee/{id}")
    public ResponseEntity<String> divideFee(@PathVariable Long id, @RequestBody Double[] arr) {
        try {
            connectionFeeService.divideFee(id, arr);
            return ResponseEntity.ok().body(
                    "Divide Successfully"
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    e.getMessage()
            );
        }
    }

    @GetMapping("/find-by-parent/{id}")
    public ResponseEntity<List<ConnectionFeeChildrenDTO>> findByParent(@PathVariable Long id) {
        try {
            return ResponseEntity.ok().body(
                    connectionFeeService.getFeesByParent(id)
            );
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

}