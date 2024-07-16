package com.example.mssqll.repository;

import com.example.mssqll.models.ExtractionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractionTaskRepository extends JpaRepository<ExtractionTask, Long> {
}
