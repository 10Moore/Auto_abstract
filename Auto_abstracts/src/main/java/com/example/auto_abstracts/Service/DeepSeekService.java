package com.example.auto_abstracts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DeepSeekService {
    private static final String DEEPSEEK_API = "https://api.deepseek.com/v1/chat/completions";
    private final String apiKey = "您的API_KEY"; // 建议放配置文件中

    @Autowired
    private RestTemplate restTemplate;

    public String generateSummary(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String requestBody = """
        {
            "model": "deepseek-chat",
            "messages": [
                {"role": "user", "content": "请用中文总结以下文本：%s"}
            ],
            "temperature": 0.7
        }
        """.formatted(text);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                DEEPSEEK_API, request, String.class);

        return parseResponse(response.getBody());
    }

    private String parseResponse(String json) {
        // 简化的JSON解析，实际应根据API响应结构调整
        return json.split("\"content\":\"")[1].split("\"")[0];
    }
}
