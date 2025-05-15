package com.example.auto_abstracts.repository;

import com.example.auto_abstracts.entity.SummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<SummaryEntity, Long> {
    Optional<SummaryEntity> findByFileId(Long fileId);
    void deleteByFileId(Long fileId);
}
