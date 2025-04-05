package com.example.auto_abstracts.Service;


import com.example.auto_abstracts.entity.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    String storeFile(MultipartFile file);
    Resource loadFile(String filename);

    List<FileEntity> getAllFiles();
    void deleteFileById(Long id);
}