package com.example.mssqll.service;

import com.example.mssqll.models.ConnectionFee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

import java.util.List;
import java.util.Optional;


public interface ConnectionFeeService {
   PagedModel<ConnectionFee> getAllFee(int page, int size);
   Optional<ConnectionFee> getFee(Long id);
   List<ConnectionFee> saveFee(Long extractionTask);
   ConnectionFee save(ConnectionFee connectionFee);
   Optional<ConnectionFee> findById(Long id);
   void deleteById(Long id);
   ConnectionFee updateFee(Long connectionFeeId, ConnectionFee connectionFeeDetails);
   PagedModel<ConnectionFee> letDoFilter(Specification<ConnectionFee> spec,int page, int size, String sortBy, String sortDir);
}