package com.example.mssqll.service.impl;


import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.models.*;
import com.example.mssqll.repository.ConnectionFeeRepository;
import com.example.mssqll.repository.ExtractionRepository;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ConnectionFeeService;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.mssqll.utiles.exceptions.ResourceNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<ConnectionFee> getAllConnectionFees() {
        return connectionFeeRepository.findAll();
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        Optional<ExtractionTask> extractionTaskOptional = extractionTaskRepository.findById(extractionTask);
        if (extractionTaskOptional.isPresent()) {
            ExtractionTask extractionTask1 = extractionTaskOptional.get();
            List<Extraction> extractions = extractionRepository.findByExtractionTask(extractionTask1);
            if (extractionTask1.getStatus().equals(FileStatus.WARNING)) {
                extractionTask1.setStatus(FileStatus.TRANSFERRED_WARNING);
            } else {
                extractionTask1.setStatus(FileStatus.TRANSFERRED_GOOD);
            }
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
                                .tax(extraction.getTax())
                                .transferPerson(userDetails)
                                .changePerson(userDetails)
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        existingFee.setStatus(connectionFeeDetails.getStatus());
        existingFee.setOrderN(connectionFeeDetails.getOrderN());
        existingFee.setRegion(connectionFeeDetails.getRegion());
        existingFee.setServiceCenter(connectionFeeDetails.getServiceCenter());
        existingFee.setWithdrawType(connectionFeeDetails.getWithdrawType());
        existingFee.setExtractionTask(connectionFeeDetails.getExtractionTask());
        existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        if (!Objects.equals(existingFee.getProjectID(), connectionFeeDetails.getProjectID())) {
            existingFee.setChangeDate(LocalDateTime.now());
            System.out.println(userDetails);
            existingFee.setChangePerson(userDetails);
        }
        existingFee.setProjectID(connectionFeeDetails.getProjectID());
        existingFee.setExtractionId(connectionFeeDetails.getExtractionId());
        existingFee.setNote(connectionFeeDetails.getNote());
        existingFee.setExtractionDate(connectionFeeDetails.getExtractionDate());
        existingFee.setTotalAmount(connectionFeeDetails.getTotalAmount());
        existingFee.setPurpose(connectionFeeDetails.getPurpose());
        existingFee.setDescription(connectionFeeDetails.getDescription());
        existingFee.setStatus(Status.TRANSFER_COMPLETE);
        if (connectionFeeDetails.getClarificationDate() == null) {
            existingFee.setClarificationDate(LocalDateTime.now());
        } else {
            existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        }
        return connectionFeeRepository.save(existingFee);
    }

    @Override
    public PagedModel<ConnectionFee> letDoFilter(Specification<ConnectionFee> spec, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return new PagedModel<>(connectionFeeRepository.findAll(spec, PageRequest.of(page, size, sort)));
    }


    @Override
    public void deleteByTaskId(Long taskId) {
        Optional<ExtractionTask> extractionTask = extractionTaskRepository.findById(taskId);

        if (extractionTask.isPresent()) {
            ExtractionTask extractionTask1 = extractionTask.get();
            extractionTask1.setStatus(FileStatus.SOFT_DELETED);
            ExtractionTask task = extractionTaskRepository.save(extractionTask1);
            connectionFeeRepository.updateStatusByExtractionTask(Status.SOFT_DELETED, extractionTask1);

        }
    }

    @Override
    public void softDeleteById(Long id) {
        ConnectionFee connectionFee = connectionFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionFee not found with id: " + id));
        connectionFee.setStatus(Status.SOFT_DELETED);
        connectionFeeRepository.save(connectionFee);
    }

    @Override
    public ByteArrayInputStream createExcel(List<ConnectionFee> connectionFees) throws IOException {
        String[] columns = {"ID", "Order Number", "Region", "Service Center", "Project ID", "Withdraw Type", "Total Amount", "Purpose", "Description", "Tax"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Connection Fees");

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        CellStyle orangeStyle = workbook.createCellStyle();
        orangeStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        orangeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle redStyle = workbook.createCellStyle();
        redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int rowIdx = 1;
        for (ConnectionFee connectionFee : connectionFees) {
            Row row = sheet.createRow(rowIdx);

            if (rowIdx == 3) {
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(getCellValue(connectionFee, i));
                    cell.setCellStyle(orangeStyle);  // Apply orange style
                }
            } else {
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(getCellValue(connectionFee, i));

                    if (i == 4) {
                        cell.setCellStyle(redStyle);
                    }
                }
            }
            rowIdx++;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }


    @SneakyThrows
    @Override
    public void divideFee(Long feeId, Double[] arr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();

        Optional<Double> arrSum = Arrays.stream(arr).reduce(Double::sum);
        Optional<ConnectionFee> connectionFee = connectionFeeRepository.findById(feeId);
        List<ConnectionFee> feeToAdd = new ArrayList<>();
        ConnectionFee connectionFeeCopy;
        ConnectionFee connectionFee1;
        if (connectionFee.isPresent()) {
            connectionFee1 = connectionFee.get();
            if (arrSum.isPresent()) {
                Double sum = arrSum.get();
                if (sum != 0.0) {
                    Double childSum = (connectionFeeRepository.sumTotalAmountByParentId(connectionFee1) != null)
                            ? connectionFeeRepository.sumTotalAmountByParentId(connectionFee1) : 0.0;
                    if (sum <= connectionFee1.getTotalAmount() && (childSum + sum) <= connectionFee1.getTotalAmount()) {
                        for (Double d : arr) {
                            if (d == 0.0) {
                                continue;
                            }
                            connectionFeeCopy = new ConnectionFee(connectionFee1);
                            connectionFeeCopy.setTotalAmount(d);
                            connectionFeeCopy.setParent(connectionFee1);
                            connectionFeeCopy.setChangePerson(userDetails);
                            connectionFeeCopy.setTransferPerson(userDetails);
                            feeToAdd.add(connectionFeeCopy);
                        }
                        try {
                            connectionFee1.setStatus(Status.CANCELD);
                            connectionFeeRepository.save(connectionFee1);
                            connectionFeeRepository.saveAll(feeToAdd);
                        } catch (Exception e) {
                            throw e;
                        }
                    } else {
                        throw new Exception("Sum of element must not be grater than parent amount");
                    }
                } else {
                    throw new Exception("Sum of array must be grater than 0");
                }
            } else {
                throw new Exception("Sum of element must be floating pont number");
            }
        } else {
            throw new ResourceNotFoundException("ConnectionFee not found with id: " + feeId);
        }
    }

    @Override
    public List<ConnectionFeeChildrenDTO> getFeesByParent(Long id) {
        Optional<ConnectionFee> connectionFee = connectionFeeRepository.findById(id);
        if (connectionFee.isPresent()) {
            List<ConnectionFee> fees = connectionFeeRepository.findAllDescendants(id);

            return fees.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            throw new ResourceNotFoundException("ConnectionFee not found with id: " + id);
        }
    }

    private ConnectionFeeChildrenDTO convertToDto(ConnectionFee connectionFee) {
        ConnectionFeeChildrenDTO dto = new ConnectionFeeChildrenDTO();
        dto.setId(connectionFee.getId());
        dto.setOrderN(connectionFee.getOrderN());
        dto.setRegion(connectionFee.getRegion());
        dto.setServiceCenter(connectionFee.getServiceCenter());
        dto.setProjectID(connectionFee.getProjectID());
        dto.setWithdrawType(connectionFee.getWithdrawType());
        dto.setClarificationDate(connectionFee.getClarificationDate());
        dto.setChangeDate(connectionFee.getChangeDate());
        dto.setTransferDate(connectionFee.getTransferDate());
        dto.setExtractionId(connectionFee.getExtractionId());
        dto.setNote(connectionFee.getNote());
        dto.setExtractionDate(connectionFee.getExtractionDate());
        dto.setTotalAmount(connectionFee.getTotalAmount());
        dto.setPurpose(connectionFee.getPurpose());
        dto.setDescription(connectionFee.getDescription());
        dto.setTax(connectionFee.getTax());

        if (connectionFee.getChildren() != null) {
            dto.setChildren(connectionFee.getChildren().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }


    private String getCellValue(ConnectionFee connectionFee, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.valueOf(connectionFee.getId());
            case 1:
                return null;
            case 2:
                return connectionFee.getOrderN();
            case 3:
                return connectionFee.getRegion();
            case 4:
                return connectionFee.getServiceCenter();
            case 5:
                return connectionFee.getProjectID();
            case 6:
                return connectionFee.getWithdrawType();
            case 7:
                return String.valueOf(connectionFee.getTotalAmount());
            case 8:
                return connectionFee.getPurpose();
            case 9:
                return connectionFee.getDescription();
            case 10:
                return connectionFee.getTax();
            default:
                return "";
        }
    }

}
