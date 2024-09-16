package com.example.mssqll.repository;

import com.example.mssqll.models.ConnectionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionFeeRepository extends JpaRepository<ConnectionFee, Long> {
    // Custom query methods can be added here if needed
}
