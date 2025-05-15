package com.example.auto_abstracts.service;

import com.example.auto_abstracts.entity.FileEntity;
import com.example.auto_abstracts.entity.SummaryEntity;
import com.example.auto_abstracts.repository.SummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SummaryService {
    private final SummaryRepository summaryRepository;

    public SummaryService(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Transactional
    public SummaryEntity saveSummary(SummaryEntity summary) {
        return summaryRepository.save(summary);
    }

    @Transactional(readOnly = true)
    public Optional<SummaryEntity> findByFileId(Long fileId) {
        return summaryRepository.findByFileId(fileId);
    }

    @Transactional
    public void deleteByFileId(Long fileId) {
        summaryRepository.deleteByFileId(fileId);
    }
}