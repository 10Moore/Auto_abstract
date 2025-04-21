package com.example.auto_abstracts.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String filepath;
    private long size;
    private String uploadTime;

    @ManyToMany
    @JsonBackReference  // 防止循环引用
    @JoinTable(
            name = "folder_file",
            joinColumns = @JoinColumn(name = "file_id"),
            inverseJoinColumns = @JoinColumn(name = "folder_id")
    )
    private Set<FolderEntity> folders = new HashSet<>();
}
