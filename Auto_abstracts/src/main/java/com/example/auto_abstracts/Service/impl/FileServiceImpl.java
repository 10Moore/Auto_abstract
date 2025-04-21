package com.example.auto_abstracts.service.impl;

import com.example.auto_abstracts.entity.FileEntity;
import com.example.auto_abstracts.entity.FolderEntity;
import com.example.auto_abstracts.repository.FileRepository;
import com.example.auto_abstracts.repository.FolderRepository;
import com.example.auto_abstracts.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FileServiceImpl implements FileService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    public FileServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("创建上传目录失败", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileEntity entity = new FileEntity();
            entity.setFilename(filename);
            entity.setFilepath(targetLocation.toString());
            entity.setSize(file.getSize());
            entity.setUploadTime(String.valueOf(LocalDateTime.now()));

            fileRepository.save(entity);
            return filename;
        } catch (IOException ex) {
            throw new RuntimeException("文件保存失败: " + file.getOriginalFilename(), ex);
        }
    }

    @Override
    public Resource loadFile(String filename) {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        return new FileSystemResource(filePath);
    }

    @Override
    public void moveFileToFolder(Long fileId, Long folderId) {

    }

    @Override
    @Transactional
    public void copyFileToFolder(Long fileId, Long folderId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在: " + fileId));

        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("文件夹不存在: " + folderId));

        // 防止重复关联
        if(file.getFolders().contains(folder)) {
            throw new RuntimeException("文件已在该文件夹中");
        }

        file.getFolders().add(folder);
        folder.getFiles().add(file);

        fileRepository.save(file);
        folderRepository.save(folder);
    }

    @Override
    @Transactional
    public void removeFileFromFolder(Long fileId, Long folderId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        file.getFolders().remove(folder);
        folder.getFiles().remove(file);

        fileRepository.save(file);
        folderRepository.save(folder);
    }

    @Override
    public List<FileEntity> findAllByFolderId(Long folderId) {
        return fileRepository.findAllByFolderId(folderId);
    }
}