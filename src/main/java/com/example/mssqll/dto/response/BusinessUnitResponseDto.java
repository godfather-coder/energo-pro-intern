package com.example.mssqll.dto.response;

import com.example.mssqll.models.BusinessUnit;
import lombok.Data;

@Data
public class BusinessUnitResponseDto {

    private Long id;
    private Integer unitNumber;
    private String name;
    private Integer unitTypeKey;
    private BusinessUnit parent;

}
