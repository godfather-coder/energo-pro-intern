package com.example.mssqll.repository;

import com.example.mssqll.models.Extraction;
import com.example.mssqll.models.ExtractionTask;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
public interface ExtractionRepository extends JpaRepository<Extraction,Long>
{
    Page<Extraction> findByStatus(int status, Pageable pageable);
    Page<Extraction> findByExtractionTaskIdAndStatus(Long ExtractionTaskId, int status, Pageable pageable);
    Page<Extraction> findByExtractionTaskId(Long ExtractionTaskId, Pageable pageable);
    @Query("SELECT SUM(e.totalAmount) FROM Extraction e WHERE e.extractionTask = :extractionTask")
    Long sumTotalAmountByExtractionTask(@Param("extractionTask") ExtractionTask extractionTask);
    @Query("SELECT SUM(e.totalAmount) FROM Extraction e")
    Long sumTotalAmount();
    Long countAllByStatus(int status);
    @Query("SELECT SUM(e.totalAmount) FROM Extraction e WHERE e.status = :status")
    Long sumTotalAmountByStatus(@Param("status") int status);
    Long countAllByExtractionTaskId(Long ExtractionTaskId);

    @Query("SELECT COUNT(e) FROM Extraction e WHERE e.extractionTask = :extractionTask AND e.status = 0")
    Long countWarningsByFileId(@Param("extractionTask") ExtractionTask extractionTask);

    @Query("SELECT COUNT(e) FROM Extraction e WHERE e.extractionTask = :extractionTask AND e.status = 1")
    Long countOkByFileId(@Param("extractionTask") ExtractionTask extractionTask);

    @Transactional
    @Modifying
    @Query("DELETE FROM Extraction e WHERE e.extractionTask = :extractionTask")
    void deleteByExtractionTask(@Param("extractionTask") ExtractionTask extractionTask);

    @Query("SELECT e FROM Extraction e WHERE e.date BETWEEN :startDate AND :endDate")
    Page<Extraction> findAllByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT  COUNT(e) FROM Extraction e WHERE e.date BETWEEN :startDate AND :endDate AND e.status = 0")
    Long countWarningsByDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT  COUNT(e) FROM Extraction e WHERE e.date BETWEEN :startDate AND :endDate AND e.status = 1")
    Long countOksByDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(e.totalAmount) FROM Extraction e WHERE e.date BETWEEN :startDate AND :endDate")
    Long sumByDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Page<Extraction> findByTotalAmount(Long totalAmount, Pageable pageable);

    @Query("SELECT e from Extraction e where e.totalAmount = :total AND e.status = :status")
    Page<Extraction> findByTotalAmountAndStatus(@Param("total") Long total, Pageable pageable,@Param("status") int status);

    @Query("SELECT COUNT(e) from Extraction e where e.totalAmount = :total AND e.status = :status")
    Long countByTotalAmountAndStatus(@Param("total") Long total, @Param("status") int status);

    @Query("SELECT SUM(e.totalAmount) from Extraction e where e.totalAmount = :total ")
    Long sumByTotalAmount(@Param("total") Long total);

    @Query("SELECT SUM(e.totalAmount) from Extraction e where e.totalAmount = :total AND e.status = :status")
    Long sumByTotalAmountAndStatus(@Param("total") Long total, @Param("status") int status);
}
