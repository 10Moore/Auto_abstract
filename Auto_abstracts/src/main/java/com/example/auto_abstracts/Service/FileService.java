
package com.example.auto_abstracts.service;

import com.example.auto_abstracts.entity.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {



    String storeFile(MultipartFile file);
    Resource loadFile(String filename);
    void moveFileToFolder(Long fileId, Long folderId);
    void copyFileToFolder(Long fileId, Long folderId);

    void removeFileFromFolder(Long fileId, Long folderId);

    // 新增带相关度评分的方法
    List<FileEntity> findAllByFolderIdWithRelevance(Long folderId);

    List<FileEntity> findAllByFolderId(Long folderId);

    // 新增方法
    default String extractTextFromPdf(String filePath) throws IOException {
        throw new UnsupportedOperationException("PDF文本提取未实现");
    }

    int calculateRelevance(Long fileId, Long folderId);

}
