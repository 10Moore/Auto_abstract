package com.example.auto_abstracts.controller;


import com.example.auto_abstracts.entity.SummaryEntity;
import com.example.auto_abstracts.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summaries")
public class SummaryController {
    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<SummaryEntity> getSummaryByFileId(@PathVariable Long fileId) {
        return summaryService.findByFileId(fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
