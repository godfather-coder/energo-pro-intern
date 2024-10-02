package com.example.mssqll.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


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
    @Nationalized
    private String orderN;

    @Column(name = "region")
    @Nationalized
    private String region;

    @Column(name = "sevice_center")
    @Nationalized
    private String serviceCenter;

    @Column(name = "project_id")
    @Nationalized
    private String projectID;

    @Column(name = "withdraw_type")
    @Nationalized
    private String withdrawType;

    @ManyToOne
    @JoinColumn(name = "extraction_task_id", nullable = false)
    private ExtractionTask extractionTask;

    @Column(name = "clarification_date")
    private LocalDateTime clarificationDate;

    @Column(name = "change_date")
    private LocalDateTime changeDate;

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
    private Double totalAmount;

    @Column(name = "purpose", length = 255, nullable = false)
    @Nationalized
    private String purpose;

    @Column(name = "description", length = 255, nullable = false)
    @Nationalized
    private String description;

    @Column(name = "tax_id", length = 255, nullable = false)
    @Nationalized
    private String tax;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id", nullable = true)
    private ConnectionFee parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ConnectionFee> children;

    public ConnectionFee() {

    }

    public ConnectionFee(String purpose, Double totalAmount, LocalDate extractionDate, Status status, LocalDateTime transferDate, ExtractionTask extractionTask, String description, Long extractionId) {
        this.purpose = purpose;
        this.totalAmount = totalAmount;
        this.extractionDate = extractionDate;
        this.status = status;
        this.transferDate = transferDate;
        this.extractionTask = extractionTask;
        this.description = description;
        this.extractionId = extractionId;
    }


    public ConnectionFee(ConnectionFee connectionFee) {
        this.status = connectionFee.getStatus();
        this.orderN = connectionFee.getOrderN();
        this.region = connectionFee.getRegion();
        this.serviceCenter = connectionFee.getServiceCenter();
        this.projectID = connectionFee.getProjectID();
        this.withdrawType = connectionFee.getWithdrawType();
        this.extractionTask = connectionFee.getExtractionTask();
        this.clarificationDate = connectionFee.getClarificationDate();
        this.changeDate = connectionFee.getChangeDate();
        this.transferDate = connectionFee.getTransferDate();
        this.extractionId = connectionFee.getExtractionId();
        this.note = connectionFee.getNote();
        this.extractionDate = connectionFee.getExtractionDate();
        this.totalAmount = connectionFee.getTotalAmount();
        this.purpose = connectionFee.getPurpose();
        this.description = connectionFee.getDescription();
        this.tax = connectionFee.getTax();
        this.parent = connectionFee.getParent();
    }
}