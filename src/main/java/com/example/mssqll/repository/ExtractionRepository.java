package com.example.mssqll.repository;

import com.example.mssqll.models.Extraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtractionRepository extends JpaRepository<Extraction,Long>
{
}
