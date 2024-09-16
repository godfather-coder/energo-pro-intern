package com.example.mssqll.repository;

import com.example.mssqll.models.ExtractionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtractionTaskRepository extends JpaRepository<ExtractionTask, Long> {
     @Query("SELECT e FROM ExtractionTask e WHERE e.fileName LIKE %:fileName%")
    List<ExtractionTask> findByFileName(@Param("fileName") String fileName);
}
