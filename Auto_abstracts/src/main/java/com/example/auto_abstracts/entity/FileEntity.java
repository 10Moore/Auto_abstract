package com.example.auto_abstracts.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("file") // 防止双向序列化
    private Set<FolderFileRating> folderRatings = new HashSet<>();

    @Transient
    private Integer relevance; // 用于前端展示

    // 获取特定文件夹相关度
    public Integer getRelevanceForFolder(Long folderId) {
        return folderRatings.stream()
                .filter(r -> r.getFolder().getId().equals(folderId))
                .findFirst()
                .map(FolderFileRating::getRelevance)
                .orElse(1);
    }

    // 添加获取相关度的方法
    public Integer getRelevance() {
        return relevance;
    }

    // 添加设置相关度的方法
    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }
}
