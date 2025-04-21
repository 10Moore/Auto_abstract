package com.example.auto_abstracts.controller;
import com.example.auto_abstracts.entity.FileEntity;
import com.example.auto_abstracts.repository.FileRepository;
import com.example.auto_abstracts.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    // ✅ 上传文件
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        String filename = fileService.storeFile(file);
        return ResponseEntity.ok(filename);
    }



    // ✅ 下载文件
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

    // ✅ 获取所有文件
    @GetMapping
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    // ✅ 删除文件（彻底从数据库中删除）
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        Optional<FileEntity> optional = fileRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        fileRepository.deleteById(id);
        return ResponseEntity.ok("已删除文件");
    }

    // ✅ 将文件移入某个文件夹（仅添加引用）
// 修改copyFileToFolder端点（第50行左右）
    @PostMapping("/{fileId}/copy-to-folder/{folderId}")  // 修正路径
    public ResponseEntity<String> copyFileToFolder(
            @PathVariable Long fileId,
            @PathVariable Long folderId) {
        try {
            fileService.copyFileToFolder(fileId, folderId);
            return ResponseEntity.ok("文件已成功关联到文件夹");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 返回具体错误信息
        }
    }

// remove-from-folder端点保持不变

    // ✅ 从某个文件夹中移除某个文件（解除关联，不删除文件）
    @PutMapping("/{fileId}/remove-from-folder/{folderId}")
    public ResponseEntity<String> removeFileFromFolder(@PathVariable Long fileId, @PathVariable Long folderId) {
        try {
            fileService.removeFileFromFolder(fileId, folderId);
            return ResponseEntity.ok("文件已移除");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("移除失败");
        }
    }
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<FileEntity>> getFilesByFolder(@PathVariable Long folderId) {
        List<FileEntity> files = fileService.findAllByFolderId(folderId);
        return ResponseEntity.ok(files);
    }
}
