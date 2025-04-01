package com.example.mssqll.service.impl;

import com.example.mssqll.models.ExtractionTask;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ExtractionTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExtractionTaskServiceImpl implements ExtractionTaskService {
    @Autowired
    private ExtractionTaskRepository extractionTaskRepository;

    @Override
    public PagedModel<ExtractionTask> getExtractionTasks(int page, int size) {
        return new PagedModel<>(extractionTaskRepository.findAllByStatusDelete(PageRequest.of(page, size)));
    }

    @Override
    public List<ExtractionTask> findByName(String name) {
        return extractionTaskRepository.findByFileName(name);
    }
}
