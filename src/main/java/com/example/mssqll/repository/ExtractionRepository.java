package com.example.mssqll.repository;

import com.example.mssqll.models.Extraction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractionRepository extends JpaRepository<Extraction,Long>
{
    Page<Extraction> findByStatus(int status, Pageable pageable);
    Page<Extraction> findByExtractionTaskIdAndStatus(Long ExtractionTaskId, int status, Pageable pageable);
    Page<Extraction> findByExtractionTaskId(Long ExtractionTaskId, Pageable pageable);
}
