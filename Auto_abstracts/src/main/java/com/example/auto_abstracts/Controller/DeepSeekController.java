package com.example.auto_abstracts.controller;

import com.example.auto_abstracts.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deepseek")
public class DeepSeekController {
    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/summarize")
    public ResponseEntity<String> summarizeText(@RequestBody String text) {
        try {
            String summary = deepSeekService.generateSummary(text);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("生成摘要失败: " + e.getMessage());
        }
    }
}
