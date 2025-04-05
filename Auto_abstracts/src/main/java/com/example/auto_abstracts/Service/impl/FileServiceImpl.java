package com.example.auto_abstracts.Service.impl;

import com.example.auto_abstracts.Service.FileService;
import com.example.auto_abstracts.entity.FileEntity;
import com.example.auto_abstracts.repository.FileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService {

    private final Path fileStorageLocation = Paths.get("upload-dir").toAbsolutePath().normalize();

    @Autowired
    private FileRepository fileRepository;

    public FileServiceImpl() throws IOException {
        Files.createDirectories(fileStorageLocation);
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            Path target = fileStorageLocation.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            FileEntity entity = new FileEntity();
            entity.setFilename(file.getOriginalFilename());
            entity.setFilepath(target.toString());
            entity.setSize(file.getSize());
            entity.setUploadTime(LocalDateTime.now());

            fileRepository.save(entity);

            return file.getOriginalFilename();
        } catch (IOException ex) {
            throw new RuntimeException("上传失败", ex);
        }
    }

    @Override
    public Resource loadFile(String filename) {
        try {
            Path filePath = fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) return resource;
            else throw new RuntimeException("文件不存在");
        } catch (Exception ex) {
            throw new RuntimeException("下载失败", ex);
        }
    }

    @Override
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    @Override
    public void deleteFileById(Long id) {
        Optional<FileEntity> file = fileRepository.findById(id);
        file.ifPresent(f -> {
            try {
                Files.deleteIfExists(Paths.get(f.getFilepath()));
                fileRepository.deleteById(id);
            } catch (IOException e) {
                throw new RuntimeException("删除失败", e);
            }
        });
    }
}
