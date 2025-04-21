
package com.example.auto_abstracts.service;

import com.example.auto_abstracts.entity.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {



    String storeFile(MultipartFile file);
    Resource loadFile(String filename);
    void moveFileToFolder(Long fileId, Long folderId);
    void copyFileToFolder(Long fileId, Long folderId);

    void removeFileFromFolder(Long fileId, Long folderId);

    List<FileEntity> findAllByFolderId(Long folderId);



}
