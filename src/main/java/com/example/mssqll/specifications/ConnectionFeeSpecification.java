package com.example.mssqll.specifications;

import com.example.mssqll.models.ConnectionFee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionFeeSpecification {

    public static Specification<ConnectionFee> getSpecifications(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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
                            predicates.add(criteriaBuilder.equal(root.get("projectID"), value));
                            break;
                        case "withdrawType":
                            predicates.add(criteriaBuilder.equal(root.get("withdrawType"), value));
                            break;
                        case "extractionTask":
                            predicates.add(criteriaBuilder.equal(root.get("extractionTask").get("id"), value));
                            break;
                        case "clarificationDate":
                            predicates.add(criteriaBuilder.equal(root.get("clarificationDate"), value));
                            break;
                        case "changeDate":
                            predicates.add(criteriaBuilder.equal(root.get("changeDate"), value));
                            break;
                        case "transferDate":
                            predicates.add(criteriaBuilder.equal(root.get("transferDate"), value));
                            break;
                        case "extractionId":
                            predicates.add(criteriaBuilder.equal(root.get("extractionId"), value));
                            break;
                        case "note":
                            predicates.add(criteriaBuilder.like(root.get("note"), "%" + value + "%"));
                            break;
                        case "extractionDate":
                            predicates.add(criteriaBuilder.equal(root.get("extractionDate"), value));
                            break;
                        case "totalAmount":
                            predicates.add(criteriaBuilder.equal(root.get("totalAmount"), value));
                            break;
                        case "purpose":
                            predicates.add(criteriaBuilder.like(root.get("purpose"), "%" + value + "%"));
                            break;
                    }
                }
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
