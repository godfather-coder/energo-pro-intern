package com.example.mssqll.service.impl;

import com.example.mssqll.dto.response.BusinessUnitResponseDto;
import com.example.mssqll.models.BusinessUnit;
import com.example.mssqll.repository.BusinessUnitRepository;
import com.example.mssqll.service.BusinessUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BusinessUnitServiceImpl implements BusinessUnitService {

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    // Save a business unit
    public BusinessUnit save(BusinessUnit businessUnit) {
        return businessUnitRepository.save(businessUnit);
    }

    // Find a business unit by ID
    public Optional<BusinessUnit> findById(Long id) {
        return businessUnitRepository.findById(id);
    }

    // Get all business units
    public List<BusinessUnit> findAll() {
        return businessUnitRepository.findAll();
    }

    // Delete a business unit by ID
    public void deleteById(Long id) {
        businessUnitRepository.deleteById(id);
    }

    // Convert BusinessUnit to BusinessUnitResponseDto
    public BusinessUnitResponseDto convertToDto(BusinessUnit businessUnit) {
        BusinessUnitResponseDto dto = new BusinessUnitResponseDto();
        dto.setId(businessUnit.getId());
        dto.setUnitNumber(businessUnit.getUnitNumber());
        dto.setName(businessUnit.getName());
        dto.setUnitTypeKey(businessUnit.getUnitTypeKey());
        dto.setParent(businessUnit.getParent()); // Include the entire parent
        return dto;
    }

    @Override
    public List<BusinessUnit> getBusinessUnitsByParent(BusinessUnit parent) {
        return businessUnitRepository.findByParent(parent);
    }

    @Override
    public List<BusinessUnit> getRootBusinessUnits() {
        return businessUnitRepository.findByParentIsNull();
    }

    @Override
    public List<?> getBykey(Integer key) {
        return businessUnitRepository.findByUnitTypeKey(key);
    }

    public List<BusinessUnitResponseDto> convertToDtoList(List<BusinessUnit> businessUnits) {
        return businessUnits.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

}
