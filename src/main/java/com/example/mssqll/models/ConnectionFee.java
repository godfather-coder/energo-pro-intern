package com.example.mssqll.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "connection_fees")
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ConnectionFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "orderN")
    private String orderN;

    @Column(name = "region")
    @Nationalized
    private String region;

    @Column(name = "sevice_center")
    @Nationalized
    private String serviceCenter;

    @Column(name = "project_id")
    private String projectID;

    @Column(name = "withdraw_type")
    private String withdrawType;

    @ManyToOne
    @JoinColumn(name = "extraction_task_id", nullable = false)
    private ExtractionTask extractionTask;

    @Column(name = "clarification_date")
    private LocalDateTime clarificationDate;

    @Column(name = "change_date")
    private LocalDateTime changeDate;

    //gadatanis taritgi +
    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    @Column(name = "exextraction_id")
    private Long extractionId;

    @Column(name = "note")
    @Nationalized
    private String note;

    @Column(name = "extraction_date")
    private LocalDate extractionDate;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "purpose", length = 255, nullable = false)
    private String purpose;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    public ConnectionFee() {

    }

    public ConnectionFee(String purpose, int totalAmount, LocalDate extractionDate, Status status, LocalDateTime transferDate, ExtractionTask extractionTask, String description, Long extractionId) {
        this.purpose = purpose;
        this.totalAmount = totalAmount;
        this.extractionDate = extractionDate;
        this.status = status;
        this.transferDate = transferDate;
        this.extractionTask = extractionTask;
        this.description = description;
        this.extractionId = extractionId;
    }
}
