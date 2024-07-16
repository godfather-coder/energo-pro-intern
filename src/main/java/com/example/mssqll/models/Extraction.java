package com.example.mssqll.models;

import com.example.mssqll.dto.enumType.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
public class Extraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date" )
    private LocalDate date;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "purpose", length = 255, nullable = false)
    private String purpose;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "status", nullable = false)
    private int status;

    @ManyToOne
    @JoinColumn(name = "extraction_task_id", nullable = false)
    private ExtractionTask extractionTask;
}
