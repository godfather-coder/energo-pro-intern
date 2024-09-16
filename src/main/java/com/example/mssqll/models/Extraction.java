package com.example.mssqll.models;

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
    private LocalDate  date;//

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;//

    @Column(name = "purpose", length = 255, nullable = false)
    private String purpose;//

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING) // Store the enum as a string in the database
    @Column(name = "status")
    private Status status;
    

    @ManyToOne
    @JoinColumn(name = "extraction_task_id", nullable = false)
    private ExtractionTask extractionTask;//
}
