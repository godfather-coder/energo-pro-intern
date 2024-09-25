package com.example.mssqll.repository;

import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.models.Status;
import com.example.mssqll.models.ExtractionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface ConnectionFeeRepository extends JpaRepository<ConnectionFee, Long>, JpaSpecificationExecutor<ConnectionFee> {
    @Transactional
    void deleteByExtractionTaskId(Long extractionTaskId);

    @Modifying
    @Transactional
    @Query("update ConnectionFee cf set cf.status = :status where cf.extractionTask = :extractionTask")
    void updateStatusByExtractionTask(Status status, ExtractionTask extractionTask);
}