package com.example.auto_abstracts.Controller;

import com.example.auto_abstracts.Service.FileService;

import com.example.auto_abstracts.entity.FileEntity;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        String filename = fileService.storeFile(file);
        return ResponseEntity.ok(filename);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename, HttpServletRequest request) {
        Resource resource = fileService.loadFile(filename);
        String contentType = request.getServletContext().getMimeType(resource.getFilename());
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @GetMapping
    public List<FileEntity> listFiles() {
        return fileService.getAllFiles();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        fileService.deleteFileById(id);
    }
}

