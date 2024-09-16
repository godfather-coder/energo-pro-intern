package com.example.mssqll.service.impl;


import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.models.Extraction;
import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.models.Status;
import com.example.mssqll.repository.ConnectionFeeRepository;
import com.example.mssqll.repository.ExtractionRepository;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ConnectionFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import com.example.mssqll.utiles.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionFeeServiceImpl implements ConnectionFeeService {
    @Autowired
    private final ConnectionFeeRepository connectionFeeRepository;
    @Autowired
    private final ExtractionRepository extractionRepository;
    @Autowired
    private final ExtractionTaskRepository extractionTaskRepository;

    public ConnectionFeeServiceImpl(ConnectionFeeRepository connectionFeeRepository,
                                    ExtractionRepository extractionRepository, ExtractionTaskRepository extractionTaskRepository) {
        this.connectionFeeRepository = connectionFeeRepository;
        this.extractionRepository = extractionRepository;
        this.extractionTaskRepository = extractionTaskRepository;
    }

    @Override
    public PagedModel<ConnectionFee> getAllFee(int page, int size) {
        return new PagedModel<>(connectionFeeRepository.findAll(PageRequest.of(page, size)));
    }

    @Override
    public Optional<ConnectionFee> getFee(Long id) {
        return connectionFeeRepository.findById(id);
    }

    @Override
    public List<ConnectionFee> saveFee(Long extractionTask) {
        Optional<ExtractionTask> extractionTaskOptional = extractionTaskRepository.findById(extractionTask);
        if (extractionTaskOptional.isPresent()) {
            ExtractionTask extractionTask1 = extractionTaskOptional.get();
            List<Extraction> extractions = extractionRepository.findByExtractionTask(extractionTask1);
            extractionTask1.setStatus(2);
            extractionTask1.setSendDate(LocalDateTime.now());
            extractionTaskRepository.save(extractionTask1);
            List<ConnectionFee> fees = new ArrayList<>();
            for (Extraction extraction : extractions) {
                fees.add(
                        ConnectionFee.builder()
                                .purpose(extraction.getPurpose())
                                .totalAmount(extraction.getTotalAmount())
                                .extractionDate(extraction.getDate())
                                .status(Status.TRANSFERRED)
                                .transferDate(LocalDateTime.now())
                                .extractionTask(extraction.getExtractionTask())
                                .description(extraction.getDescription())
                                .extractionId(extraction.getId())
                                .build()
                );
            }

            return connectionFeeRepository.saveAll(fees);
        } else {
            throw new ResourceNotFoundException("Extraction task not found");
        }

    }

    @Override
    public ConnectionFee save(ConnectionFee connectionFee) {
        connectionFee.setTransferDate(LocalDateTime.now());
        return connectionFeeRepository.save(connectionFee);
    }

    @Override
    public Optional<ConnectionFee> findById(Long id) {
        return connectionFeeRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        connectionFeeRepository.deleteById(id);
    }

    @Override
    public ConnectionFee updateFee(Long connectionFeeId, ConnectionFee connectionFeeDetails) {
        ConnectionFee existingFee = connectionFeeRepository.findById(connectionFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionFee not found with id: " + connectionFeeId));

        existingFee.setStatus(connectionFeeDetails.getStatus());
        existingFee.setOrderN(connectionFeeDetails.getOrderN());
        existingFee.setRegion(connectionFeeDetails.getRegion());
        existingFee.setServiceCenter(connectionFeeDetails.getServiceCenter());
        existingFee.setProjectID(connectionFeeDetails.getProjectID());
        existingFee.setWithdrawType(connectionFeeDetails.getWithdrawType());
        existingFee.setExtractionTask(connectionFeeDetails.getExtractionTask());
        existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        existingFee.setChangeDate(LocalDateTime.now());
        existingFee.setExtractionId(connectionFeeDetails.getExtractionId());
        existingFee.setNote(connectionFeeDetails.getNote());
        existingFee.setExtractionDate(connectionFeeDetails.getExtractionDate());
        existingFee.setTotalAmount(connectionFeeDetails.getTotalAmount());
        existingFee.setPurpose(connectionFeeDetails.getPurpose());
        existingFee.setDescription(connectionFeeDetails.getDescription());
        existingFee.setStatus(Status.TRANSFER_COMPLETE);
        if (connectionFeeDetails.getClarificationDate()==null) {
            existingFee.setClarificationDate(LocalDateTime.now());
        } else {
            existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        }
        return connectionFeeRepository.save(existingFee);
    }

    @Override
    public PagedModel<ConnectionFee> letDoFilter(Specification<ConnectionFee> spec,int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return new PagedModel<>( connectionFeeRepository.findAll(spec,PageRequest.of(page, size,sort)));
    }

}
