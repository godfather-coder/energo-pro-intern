package com.example.mssqll.service;

import com.example.mssqll.models.ConnectionFee;

import java.util.List;
import java.util.Optional;

public interface ConnectionFeeService {
    List<ConnectionFee> findAll();
    Optional<ConnectionFee> findById(Long id);
    ConnectionFee save(ConnectionFee connectionFee);
    void deleteById(Long id);
}