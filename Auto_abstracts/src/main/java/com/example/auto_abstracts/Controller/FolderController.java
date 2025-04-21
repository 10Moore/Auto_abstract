package com.example.auto_abstracts.controller;

import com.example.auto_abstracts.entity.FolderEntity;
import com.example.auto_abstracts.repository.FolderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;

    @GetMapping
    public List<FolderEntity> getAllFolders() {
        List<FolderEntity> folders = folderRepository.findAll();
        System.out.println("返回的文件夹数据: " + folders);
        return folders;
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@RequestBody Map<String, Object> requestBody) {
        try {
            FolderEntity folder = new FolderEntity();

            // 设置必填字段
            folder.setName(requestBody.getOrDefault("name", "新建文件夹").toString());
            folder.setAttribute(requestBody.getOrDefault("attribute", "未分类").toString());
            folder.setCreateTime(LocalDateTime.now());

            // 调试日志
            // 创建文件夹时输出具体字段
            System.out.println("Creating folder: " + folder.getName() + ", " + folder.getAttribute() + ", " + folder.getCreateTime());


            FolderEntity saved = folderRepository.save(folder);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "CREATE_FAILED",
                    "message", e.getMessage()
            ));
        }
    }


    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateFolder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {  // 改用更宽松的Map接收

        try {
            // 调试日志
            System.out.println("Received update request for folder " + id + ": " + requestBody);

            FolderEntity folder = folderRepository.findById(id)
                    .orElseThrow(() -> {
                        System.out.println("Folder not found: " + id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "文件夹不存在");
                    });

            // 安全更新字段
            requestBody.forEach((key, value) -> {
                switch (key) {
                    case "name":
                        folder.setName(value.toString());
                        break;
                    case "attribute":
                        folder.setAttribute(value.toString());
                        break;
                    default:
                        System.out.println("Ignored unknown field: " + key);
                }
            });

            FolderEntity updated = folderRepository.save(folder);
            System.out.println("Update successful: " + updated);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", updated
            ));

        } catch (ResponseStatusException e) {
            throw e; // 重新抛出已知异常
        } catch (Exception e) {
            System.err.println("Update failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "UPDATE_FAILED",
                    "message", e.getMessage(),
                    "details", requestBody
            ));
        }
    }

    @DeleteMapping("/{id}")
    public void deleteFolder(@PathVariable Long id) {
        folderRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFolderName(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        if (!payload.containsKey("name")) {
            return ResponseEntity.badRequest().body("必须提供name字段");
        }

        FolderEntity folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        folder.setName(payload.get("name"));
        folderRepository.save(folder);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "newName", folder.getName()
        ));
    }
}