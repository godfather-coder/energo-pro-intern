package com.example.mssqll.models;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
public class ExtractionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    private String fileName;

    private int status;

    public ExtractionTask(LocalDateTime date, String name, int status) {
        this.date = date;
        this.fileName = name;
        this.status = status;
    }
}