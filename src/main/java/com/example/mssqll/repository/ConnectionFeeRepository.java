package com.example.mssqll.repository;

import com.example.mssqll.models.ConnectionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface ConnectionFeeRepository extends JpaRepository<ConnectionFee, Long>, JpaSpecificationExecutor<ConnectionFee> {
    @Transactional
    void deleteByExtractionTaskId(Long extractionTaskId);
}