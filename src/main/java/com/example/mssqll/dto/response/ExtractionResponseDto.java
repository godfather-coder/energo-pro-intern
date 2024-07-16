package com.example.mssqll.dto.response;

import com.example.mssqll.dto.enumType.Status;
import com.example.mssqll.models.ExtractionTask;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;


@Data
@Builder
public class ExtractionResponseDto {

    private Long id;

    private LocalDate date;

    private int totalAmount;

    private String purpose;

    private String description;

    private int status;

    private ExtractionTask extractionTask;


}
