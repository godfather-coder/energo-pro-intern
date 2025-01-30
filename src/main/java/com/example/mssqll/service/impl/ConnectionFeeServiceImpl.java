package com.example.mssqll.service.impl;


import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.dto.response.ConnectionFeeResponseDto;
import com.example.mssqll.dto.response.UserResponseDto;
import com.example.mssqll.models.*;
import com.example.mssqll.repository.ConnectionFeeRepository;
import com.example.mssqll.repository.ExtractionRepository;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ConnectionFeeService;
import com.example.mssqll.utiles.exceptions.FileAlreadyTransferredException;
import com.example.mssqll.utiles.exceptions.ResourceNotFoundException;
import lombok.SneakyThrows;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        if (extractionTaskOptional.isEmpty()) {
            throw new ResourceNotFoundException("Extraction task not found");
        }

        ExtractionTask extractionTask1 = extractionTaskOptional.get();
        if (extractionTask1.getStatus() == FileStatus.TRANSFERRED_GOOD ||
                extractionTask1.getStatus() == FileStatus.TRANSFERRED_WARNING) {
            throw new FileAlreadyTransferredException("file with id: " + extractionTask1.getId() + " already transferred");
        }

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
                            .orderStatus(OrderStatus.ORDER_INCOMPLETE)
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
    public ConnectionFee updateFee(Long connectionFeeId, ConnectionFee connectionFeeDetails) {
        ConnectionFee existingFee = connectionFeeRepository.findById(connectionFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionFee not found with id: " + connectionFeeId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        existingFee.setStatus(connectionFeeDetails.getStatus());
        existingFee.setRegion(connectionFeeDetails.getRegion());
        existingFee.setServiceCenter(connectionFeeDetails.getServiceCenter());
        existingFee.setWithdrawType(connectionFeeDetails.getWithdrawType());
        existingFee.setExtractionTask(connectionFeeDetails.getExtractionTask());
        existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        if (!Objects.equals(existingFee.getProjectID(), connectionFeeDetails.getProjectID())) {
            existingFee.setChangeDate(LocalDateTime.now());
            existingFee.setChangePerson(userDetails);
        }
        existingFee.setExtractionId(connectionFeeDetails.getExtractionId());
        existingFee.setNote(connectionFeeDetails.getNote());
        existingFee.setExtractionDate(connectionFeeDetails.getExtractionDate());
        existingFee.setTotalAmount(connectionFeeDetails.getTotalAmount());
        existingFee.setPurpose(connectionFeeDetails.getPurpose());
        existingFee.setDescription(connectionFeeDetails.getDescription());
        existingFee.setPaymentOrderSentDate(connectionFeeDetails.getPaymentOrderSentDate());//new
        existingFee.setTreasuryRefundDate(connectionFeeDetails.getTreasuryRefundDate());//new
        if (!Objects.equals(existingFee.getProjectID(), connectionFeeDetails.getProjectID())) {
            List<String> proj = existingFee.getCanceledProject();
            if (!proj.isEmpty()) {
                if (connectionFeeDetails.getProjectID() != proj.get(proj.size() - 1)) {
                    proj.add(existingFee.getProjectID());
                }
            } else {
                proj.add(existingFee.getProjectID());
            }
            existingFee.setCanceledProject(proj);
            existingFee.setProjectID(connectionFeeDetails.getProjectID());
        }
        if (connectionFeeDetails.getOrderStatus() == OrderStatus.CANCELED) {
            List<String> proj = existingFee.getCanceledProject();
            if (!proj.isEmpty()) {
                if (!Objects.equals(connectionFeeDetails.getProjectID(), proj.get(proj.size() - 1))) {
                    proj.add(existingFee.getProjectID());
                }
            } else {
                proj.add(existingFee.getProjectID());
            }

            existingFee.setCanceledProject(proj);
        }
        existingFee.setProjectID(connectionFeeDetails.getProjectID());

        if (!Objects.equals(existingFee.getOrderN(), connectionFeeDetails.getOrderN())) {
            List<String> newLst = existingFee.getCanceledOrders();
            newLst.add(existingFee.getOrderN());
            existingFee.setCanceledOrders(newLst);//new
            existingFee.setOrderN(connectionFeeDetails.getOrderN());
        }
        existingFee.setStatus(Status.TRANSFER_COMPLETE);
        if (connectionFeeDetails.getClarificationDate() == null) {
            existingFee.setClarificationDate(LocalDateTime.now());
        } else {
            existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        }
        existingFee.setOrderStatus(connectionFeeDetails.getOrderStatus());
        return connectionFeeRepository.save(existingFee);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        Optional<ExtractionTask> extractionTask = extractionTaskRepository.findById(taskId);

        if (extractionTask.isPresent()) {
            ExtractionTask extractionTask1 = extractionTask.get();
            extractionTask1.setStatus(FileStatus.SOFT_DELETED);
            connectionFeeRepository.updateStatusByExtractionTask(Status.SOFT_DELETED, extractionTask1);

        }
    }

    @Override
    public void softDeleteById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        ConnectionFee connectionFee = connectionFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionFee not found with id: " + id));
        List<ConnectionFee> connectionFees = connectionFeeRepository.findAllDescendants(connectionFee.getParent().getId());
        if (Objects.equals(connectionFeeRepository.sumTotalAmountByParentId(connectionFee.getParent()), connectionFee.getParent().getTotalAmount())) {
            connectionFee.setNote("ნაშთი");
            connectionFee.setStatus(Status.REMINDER);
            connectionFee.setOrderN("ნაშთი");
            connectionFeeRepository.save(connectionFee);
        } else if (!connectionFee.getStatus().equals(Status.REMINDER)) {
            Optional<ConnectionFee> reminderFee = connectionFeeRepository.findReminderChildByParentId(connectionFee.getParent().getId());
            if (reminderFee.isPresent()) {
                ConnectionFee reminderFee1 = reminderFee.get();
                reminderFee1.setTotalAmount(reminderFee1.getTotalAmount() + connectionFee.getTotalAmount());
            }
            connectionFee.setStatus(Status.SOFT_DELETED);
            connectionFee.setChangePerson(userDetails);
            connectionFeeRepository.save(connectionFee);
        } else if (connectionFees.size() == 1) {
            connectionFees.get(0).setStatus(Status.SOFT_DELETED);
            connectionFees.get(0).setChangePerson(userDetails);
            connectionFeeRepository.save(connectionFees.get(0));
            ConnectionFee parent = connectionFee.getParent();
            if (parent.getProjectID() != null) {
                parent.setStatus(Status.TRANSFER_COMPLETE);
            } else {
                parent.setStatus(Status.TRANSFERRED);
            }
            connectionFeeRepository.save(parent);
        }

    }

    @Override
    public ByteArrayInputStream createExcel(List<ConnectionFee> connectionFees) throws IOException {
        String[] columns = {
                "ID", //0
                "ორდერის N",//1
                "რეგიონი",//2
                "სერვის ცენტრი",//3
                "პროექტის ნომერი",//4
                "გარკვევის თარიღი",//5
                "შეცვლის თარიღი",//6
                "შენიშვნა",//7
                "გადმოტანის თარიღი",//8
                "ჩარიცხვის თარიღი",//9
                "თანხა",//10
                "გადამხდელის იდენტიფიკატორი",//11
                "მიზანი",//12
                "აღწერა",//13
                "შემცველელი",//14
                "გაუქმებული პროექტები",//15
        };

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Connection Fees");

        Row headerRow = sheet.createRow(0);
        XSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        XSSFCellStyle boldCellStyle = workbook.createCellStyle();
        boldCellStyle.setFont(boldFont);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(boldCellStyle);
        }

        int rowIdx = 1;
        for (ConnectionFee connectionFee : connectionFees) {
            Row row = sheet.createRow(rowIdx);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(getCellValue(connectionFee, i));
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
                        Optional<ConnectionFee> reminderChildOpt = connectionFeeRepository.findReminderChildByParentId(connectionFee1.getId());
                        boolean reminderUpdated = false;
                        if (reminderChildOpt.isPresent()) {
                            ConnectionFee reminderChild = reminderChildOpt.get();
                            double reminderAmount = reminderChild.getTotalAmount();
                            double newReminderAmount = reminderAmount - sum;

                            if (newReminderAmount >= 0) {
                                reminderChild.setTotalAmount(newReminderAmount);
                                connectionFeeRepository.save(reminderChild);
                                reminderUpdated = true;
                            } else {
                                throw new Exception("Insufficient amount in Reminder child for this operation.");
                            }
                        }
                        int childNum = 1;
                        double newElement = connectionFee1.getTotalAmount() - childSum - sum;
                        Double[] newArr = Arrays.copyOf(arr, arr.length + (reminderUpdated ? 0 : 1));
                        if (!reminderUpdated) {
                            newArr[newArr.length - 1] = newElement;
                        }
                        for (Double d : newArr) {
                            if (d == 0.0) {
                                continue;
                            }
                            connectionFeeCopy = new ConnectionFee(connectionFee1);
                            connectionFeeCopy.setTotalAmount(d);
                            connectionFeeCopy.setParent(connectionFee1);
                            connectionFeeCopy.setChangePerson(userDetails);
                            connectionFeeCopy.setTransferPerson(userDetails);
                            connectionFeeCopy.setOrderStatus(OrderStatus.ORDER_INCOMPLETE);
                            connectionFeeCopy.setStatus(Status.TRANSFERRED);

                            String parentQueueNumber = connectionFee1.getQueueNumber() != null
                                    ? connectionFee1.getQueueNumber()
                                    : String.valueOf(connectionFee1.getId());
                            connectionFeeCopy.setQueueNumber(parentQueueNumber + "-" + (connectionFeeRepository.childNumberByParentId(feeId) + childNum));
                            childNum++;
                            feeToAdd.add(connectionFeeCopy);
                        }

                        boolean isLastElement = feeToAdd.get(feeToAdd.size() - 1).getTotalAmount() == newElement;
                        boolean isFullSumMatch = (sum + childSum == connectionFee1.getTotalAmount());
                        if (!reminderUpdated && !isFullSumMatch && isLastElement) {
                            ConnectionFee lastFee = feeToAdd.get(feeToAdd.size() - 1);
                            lastFee.setNote("test2");
                            lastFee.setOrderN("ნაშთი");
                            lastFee.setDescription("ნაშთი");
                            lastFee.setPurpose("ნაშთი");
                            lastFee.setStatus(Status.REMINDER);
                        } else if (isFullSumMatch) {
                            connectionFeeRepository.deleteResidualEntriesByParentId(connectionFee1.getId());
                        }
                        connectionFee1.setStatus(Status.CANCELED);
                        connectionFeeRepository.save(connectionFee1);
                        connectionFeeRepository.saveAll(feeToAdd);
                    } else {
                        throw new Exception("Sum of elements must not be greater than parent amount");
                    }
                } else {
                    throw new Exception("Sum of array must be greater than 0");
                }
            } else {
                throw new Exception("Sum of elements must be a floating-point number");
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

    @Override
    public List<ConnectionFee> getDownloadDataBySpec(Specification<ConnectionFee> spec) {
        return connectionFeeRepository.findAll(spec);
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
        return switch (columnIndex) {
            case 0 -> String.valueOf(connectionFee.getId());
            case 1 -> connectionFee.getOrderN();
            case 2 -> connectionFee.getRegion();
            case 3 -> connectionFee.getServiceCenter();
            case 4 -> connectionFee.getProjectID();
            case 5 -> {
                if (connectionFee.getClarificationDate() != null) {
                    yield connectionFee.getClarificationDate().toString();
                }
                yield "";
            }
            case 6 -> {
                if (connectionFee.getChangeDate() != null) {
                    yield connectionFee.getChangeDate().toString();
                }
                yield "";
            }
            case 7 -> connectionFee.getNote();
            case 8 -> connectionFee.getTransferDate().toString();
            case 9 -> connectionFee.getExtractionDate().toString();
            case 10 -> connectionFee.getTotalAmount().toString();
            case 11 -> connectionFee.getTax();
            case 12 -> connectionFee.getPurpose();
            case 13 -> connectionFee.getDescription();
            case 14 ->
                    connectionFee.getChangePerson().getLastName() + " " + connectionFee.getChangePerson().getFirstName();
            case 15 -> connectionFee.getCanceledOrders().toString();
            default -> "";
        };
    }

    @Override
    public PagedModel<?> letDoFilter(Specification<ConnectionFee> spec, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return new PagedModel<>(castToDtos(connectionFeeRepository.findAll(spec, PageRequest.of(page, size, sort))));
    }

    private Page<ConnectionFeeResponseDto> castToDtos(Page<ConnectionFee> page) {
        List<ConnectionFeeResponseDto> cfDtos = page.getContent()
                .stream()
                .map(this::castToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(cfDtos, page.getPageable(), page.getTotalElements());
    }

    private ConnectionFeeResponseDto castToDto(ConnectionFee cf) {
        ConnectionFeeResponseDto cfd = baseCast(cf);
        List<ConnectionFeeResponseDto> emptyDto = new ArrayList<>();
        List<ConnectionFeeResponseDto> cfDtos = new ArrayList<>();
        if (!cf.getChildren().isEmpty()) {
            ConnectionFeeResponseDto cfChild1;
            for (ConnectionFee cfChild : cf.getChildren()) {
                cfChild1 = baseCast(cfChild);
                cfChild1.setChildren(emptyDto);
                cfDtos.add(cfChild1);
            }
        }
        cfd.setChildren(cfDtos);
        return cfd;
    }

    private ConnectionFeeResponseDto baseCast(ConnectionFee cf) {
        return ConnectionFeeResponseDto.builder()
                .id(cf.getId())
                .orderStatus(cf.getOrderStatus())
                .status(cf.getStatus())
                .orderN(cf.getOrderN())
                .region(cf.getRegion())
                .serviceCenter(cf.getServiceCenter())
                .queueNumber(cf.getQueueNumber())
                .projectID(cf.getProjectID())
                .withdrawType(cf.getWithdrawType())
                .clarificationDate(cf.getClarificationDate())
                .treasuryRefundDate(cf.getTreasuryRefundDate())
                .paymentOrderSentDate(cf.getPaymentOrderSentDate())
                .canceledOrders(cf.getCanceledOrders())
                .canceledProject(cf.getCanceledProject())
                .changeDate(cf.getChangeDate())
                .transferDate(cf.getTransferDate())
                .extractionDate(cf.getExtractionDate())
                .extractionTask(cf.getExtractionTask())
                .totalAmount(cf.getTotalAmount())
                .purpose(cf.getPurpose())
                .description(cf.getDescription())
                .tax(cf.getTax())
                .transferPerson(castUserToDto(cf.getTransferPerson()))
                .changePerson(castUserToDto(cf.getChangePerson()))
                .build();
    }

    private UserResponseDto castUserToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public Integer uploadHistory(MultipartFile file) throws IOException {
        LocalDateTime today = LocalDateTime.now();
        ExtractionTask task;
        try {
            task = extractionTaskRepository.save(new ExtractionTask(today, file.getOriginalFilename(), FileStatus.HISTORY));
        } catch (Exception e) {
            return 0;
        }
        Map<Integer, String> PAYMENT_MAPPING = new HashMap<>();
        PAYMENT_MAPPING.put(1, "1 (პირველი გადახდა)");
        PAYMENT_MAPPING.put(2, "2 (მეორე გადახდა)");
        PAYMENT_MAPPING.put(3, "3 (სრული საფასურის გადახდა)");
        PAYMENT_MAPPING.put(4, "4 (ერთანი გადახდა, გადანაწილებული რამოდენიმე პროექტის საფასურად)");
        PAYMENT_MAPPING.put(5, "5 (სავარაუდოდ არაა ახალი მიერთების საფასური)");
        PAYMENT_MAPPING.put(6, "6 (თანხის დაბრუნება)");
        PAYMENT_MAPPING.put(7, "7 (გადანაწილებული გადახდა / რამოდენიმეჯერ გადახდა)");
        PAYMENT_MAPPING.put(8, "8 (სააბონენტო ბარათზე თანხის დასმა)");
        PAYMENT_MAPPING.put(9, "9 (ხაზის მშენებლობა / არარეგულირებული პროექტები (პირველი ან სრული გადახდა))");
        PAYMENT_MAPPING.put(10, "10 (სისტემის ნებართვის საფასური)");
        PAYMENT_MAPPING.put(19, "19 (ხაზის მშენებლობა / არარეგულირებული პროექტები (მეორე გადახდა))");
        PAYMENT_MAPPING.put(11, "11 (სააბონენტო ბარათიდან თანხის გადმოტანა)");
        PAYMENT_MAPPING.put(12, "12 (ჯარიმის გადატანა)");
        PAYMENT_MAPPING.put(13, "13 (საპროექტო ტრასის შეტანხმება)");
        PAYMENT_MAPPING.put(14, "14 (ჰესები DDSH)");
        PAYMENT_MAPPING.put(15, "15 (ჰესები DDNA)");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        List<ConnectionFee> connectionFees = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) { // Start from row 2 to skip headers
                Row row = sheet.getRow(i);
                ConnectionFee fee = new ConnectionFee();
                fee.setId((getLongCellValue(row.getCell(0))));
                fee.setOrderN(getStringCellValue(row.getCell(1)));
                fee.setRegion(getStringCellValue(row.getCell(2)));
                fee.setServiceCenter(getStringCellValue(row.getCell(3)));
                fee.setProjectID(getStringCellValue(row.getCell(4)));
                Integer paymentType = (int) row.getCell(5).getNumericCellValue();
                fee.setWithdrawType(PAYMENT_MAPPING.getOrDefault(paymentType, "Unknown payment type"));
                LocalDate date = LocalDate.parse(row.getCell(6).toString(), formatter);
                fee.setExtractionDate(date);
                fee.setTotalAmount(getDoubleCellValue(row.getCell(7)));
                fee.setPurpose(getStringCellValue(row.getCell(8)));
                fee.setDescription(getStringCellValue(row.getCell(9)));
                fee.setTax(getStringCellValue(row.getCell(10)));
                fee.setNote(getStringCellValue(row.getCell(12)));

                if (row.getCell(11) != null && !row.getCell(11).toString().isEmpty()) {
                    try {
                        LocalDate clarificationdate = LocalDate.parse(row.getCell(11).toString(), formatter);
                        LocalDateTime clarificationDate = clarificationdate.atStartOfDay();
                        fee.setClarificationDate(clarificationDate);
                    } catch (Exception e) {
                        System.err.println("Error parsing date: " + row.getCell(11).toString());
                        System.out.println("Line: " + row.getRowNum()+" Col"+11);
                        System.out.println(e.getMessage());
                        fee.setClarificationDate(null);
                    }
                } else {
                    fee.setClarificationDate(null);
                }
                if (row.getCell(13) != null && !row.getCell(13).toString().isEmpty()) {
                    try {
                        LocalDate treasuryRefundDate = LocalDate.parse(row.getCell(13).toString(), formatter);
                        fee.setTreasuryRefundDate(treasuryRefundDate);
                    } catch (Exception e) {
                        System.err.println("Error parsing date: " + row.getCell(13).toString());
                        System.out.println("Line: " + row.getRowNum()+" Col"+13);
                        System.out.println(e.getMessage());

                        fee.setTreasuryRefundDate(null);
                    }
                } else {
                    fee.setTreasuryRefundDate(null);
                }
                if (row.getCell(14) != null && !row.getCell(14).toString().isEmpty()) {
                    try {
                        LocalDate PaymentOrderSentDate = LocalDate.parse(row.getCell(14).toString(), formatter);
                        fee.setPaymentOrderSentDate(PaymentOrderSentDate);
                    } catch (Exception e) {
                        System.err.println("Error parsing date: " + row.getCell(14).toString());
                        System.out.println("Line: " + row.getRowNum()+" Col"+14);
                        System.out.println(e.getMessage());

                        fee.setPaymentOrderSentDate(null);
                    }
                } else {
                    fee.setPaymentOrderSentDate(null);
                }
//                fee.setOrderStatus(OrderStatus.valueOf(getStringCellValue(row.getCell(1))));
                List<String> canceledProjects = new ArrayList<>();
                canceledProjects.add(row.getCell(16).toString());
                canceledProjects.add(row.getCell(17).toString());
                canceledProjects.add(row.getCell(18).toString());
                fee.setCanceledProject(canceledProjects);
                fee.setStatus(Status.TRANSFERRED);
                fee.setTransferDate(LocalDateTime.now());
                fee.setExtractionId(0L);
                fee.setTransferPerson(userDetails);
                fee.setChangePerson(userDetails);
                fee.setExtractionTask(task);
                connectionFees.add(fee);

            }
            try {
                try {
                    connectionFees.forEach(System.out::println);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Processed " + connectionFees.size() + " records");
        return connectionFees.size();
    }

    // Helper Methods
    private static String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private static Long getLongCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Long.parseLong(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null; // Or throw an exception if this is critical
            }
        }
        return null;
    }

    private static Double getDoubleCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null; // Or throw an exception if this is critical
            }
        }
        return null;
    }


}


