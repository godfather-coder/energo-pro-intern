package com.example.mssqll.service;

import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.models.ConnectionFee;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface ConnectionFeeService {
   List<ConnectionFee> getAllConnectionFees();
   PagedModel<ConnectionFee> getAllFee(int page, int size);
   Optional<ConnectionFee> getFee(Long id);
   List<ConnectionFee> saveFee(Long extractionTask);
   ConnectionFee save(ConnectionFee connectionFee);
   Optional<ConnectionFee> findById(Long id);
   void deleteById(Long id);
   ConnectionFee updateFee(Long connectionFeeId, ConnectionFee connectionFeeDetails);
   PagedModel<ConnectionFee> letDoFilter(Specification<ConnectionFee> spec, int page, int size, String sortBy, String sortDir);
   void deleteByTaskId(Long taskId);
   void softDeleteById(Long id);
   ByteArrayInputStream createExcel(List<ConnectionFee> connectionFees) throws IOException;
   void divideFee(Long feeId,Double[] arr);
   List<ConnectionFeeChildrenDTO> getFeesByParent(Long id);
}