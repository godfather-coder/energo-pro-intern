package com.example.mssqll.repository;

import com.example.mssqll.dto.enumType.Status;
import com.example.mssqll.models.Extraction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ExtractionRepository extends JpaRepository<Extraction,Long>
{
    Page<Extraction> findByStatus(Status status, Pageable pageable);
}
