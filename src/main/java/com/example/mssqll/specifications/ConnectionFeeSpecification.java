package com.example.mssqll.specifications;

import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.models.ExtractionTask;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionFeeSpecification {

    public static Specification<ConnectionFee> getSpecifications(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("parent")));

            if (!filters.containsKey("status")) {
                predicates.add(criteriaBuilder.notEqual(root.get("status"), "SOFT_DELETED"));
            }

            filters.forEach((key, value) -> {
                if (value != null) {
                    switch (key) {
                        case "status":
                            predicates.add(criteriaBuilder.equal(root.get("status"), value));
                            break;
                        case "orderN":
                            predicates.add(criteriaBuilder.like(root.get("orderN"), "%" + value + "%"));
                            break;
                        case "region":
                            predicates.add(criteriaBuilder.like(root.get("region"), "%" + value + "%"));
                            break;
                        case "serviceCenter":
                            predicates.add(criteriaBuilder.like(root.get("serviceCenter"), "%" + value + "%"));
                            break;
                        case "projectID":
                            predicates.add(criteriaBuilder.like(root.get("projectID"), "%" + value + "%"));
                            break;
                        case "withdrawType":
                            predicates.add(criteriaBuilder.like(root.get("withdrawType"), "%" + value + "%"));
                            break;
                        case "extractionTask":
                            predicates.add(criteriaBuilder.equal(root.get("extractionTask").get("id"), value));
                            break;
                        case "clarificationDateStart":
                            LocalDateTime clarificationDateStart = parseToLocalDateTime(value.toString());
                            if (clarificationDateStart != null) {
                                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("clarificationDate"), clarificationDateStart));
                            }
                            break;
                        case "clarificationDateEnd":
                            LocalDateTime clarificationDateEnd = parseToLocalDateTime(value.toString());
                            if (clarificationDateEnd != null) {
                                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("clarificationDate"), clarificationDateEnd));
                            }
                            break;
                        case "changeDateStart":
                            LocalDateTime changeDateStart = parseToLocalDateTime(value.toString());
                            if (changeDateStart != null) {
                                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("changeDate"), changeDateStart));
                            }
                            break;
                        case "changeDateEnd":
                            LocalDateTime changeDateEnd = parseToLocalDateTime(value.toString());
                            if (changeDateEnd != null) {
                                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("changeDate"), changeDateEnd));
                            }
                            break;
                        case "transferDateStart":
                            LocalDateTime transferDateStart = parseToLocalDateTime(value.toString());
                            if (transferDateStart != null) {
                                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transferDate"), transferDateStart));
                            }
                            break;
                        case "transferDateEnd":
                            LocalDateTime transferDateEnd = parseToLocalDateTime(value.toString());
                            if (transferDateEnd != null) {
                                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transferDate"), transferDateEnd));
                            }
                            break;
                        case "extractionId":
                            predicates.add(criteriaBuilder.equal(root.get("extractionId"), value));
                            break;
                        case "note":
                            predicates.add(criteriaBuilder.like(root.get("note"), "%" + value + "%"));
                            break;
                        case "extractionDateStart":
                            LocalDate extractionDateStart = LocalDate.parse(value.toString());
                            if (extractionDateStart != null) {
                                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("extractionDate"), extractionDateStart));
                            }
                            break;
                        case "extractionDateEnd":
                            LocalDate extractionDateEnd = LocalDate.parse(value.toString());
                            if (extractionDateEnd != null) {
                                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("extractionDate"), extractionDateEnd));
                            }
                            break;
                        case "totalAmountStart":
                            Double totalAmountStart = Double.parseDouble(value.toString());
                            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), totalAmountStart));
                            break;
                        case "totalAmountEnd":
                            Double totalAmountEnd = Double.parseDouble(value.toString());
                            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), totalAmountEnd));
                            break;
                        case "purpose":
                            predicates.add(criteriaBuilder.like(root.get("purpose"), "%" + value + "%"));
                            break;
                        case "description":
                            predicates.add(criteriaBuilder.like(root.get("description"), "%" + value + "%"));
                            break;
                        case "tax":
                            predicates.add(criteriaBuilder.like(root.get("tax"), "%" + value + "%"));
                            break;
                        case "file":
                            Join<ConnectionFee, ExtractionTask> extractionTaskJoin = root.join("extractionTask");
                            predicates.add(criteriaBuilder.like(extractionTaskJoin.get("fileName"), "%" + value + "%"));
                            break;
                    }
                }
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    private static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"); // Adjust pattern as necessary
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException(e.getMessage(), dateTimeStr, 0);
        }
    }
}
