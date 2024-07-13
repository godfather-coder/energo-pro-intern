package com.example.mssqll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MissingFieldInfo {
    private int rowNumber;
    private String rowContent;
    private String missingFields;
}