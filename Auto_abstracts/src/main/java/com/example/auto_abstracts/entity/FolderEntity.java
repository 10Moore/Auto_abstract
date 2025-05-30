package com.example.auto_abstracts.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "folders")
public class FolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自动生成ID
    private Long id;


    private String name;

    private String attribute;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @ManyToMany(mappedBy = "folders")
    @JsonManagedReference  // 防止循环引用
    private Set<FileEntity> files = new HashSet<>();  // 确保初始化为非null的集合
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("folder") // 防止双向序列化
    private Set<FolderFileRating> fileRatings = new HashSet<>();


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Set<FileEntity> getFiles() {
        return files;  // 返回初始化好的集合
    }

    public void setFiles(Set<FileEntity> files) {
        this.files = files;
    }

    // 添加获取和设置相关度的方法
    public Integer getFileRelevance(Long fileId) {
        return fileRatings.stream()
                .filter(r -> r.getFile().getId().equals(fileId))
                .findFirst()
                .map(FolderFileRating::getRelevance)
                .orElse(1); // 默认1星
    }

    public void setFileRelevance(FileEntity file, Integer relevance) {
        FolderFileRating rating = fileRatings.stream()
                .filter(r -> r.getFile().equals(file))
                .findFirst()
                .orElseGet(() -> {
                    FolderFileRating newRating = new FolderFileRating();
                    newRating.setFolder(this);
                    newRating.setFile(file);
                    fileRatings.add(newRating);
                    return newRating;
                });
        rating.setRelevance(relevance);
    }

    public Set<FolderFileRating> getFileRatings() {
        return fileRatings;
    }

    @PostLoad
    private void init() {
        if (files == null) {
            files = new HashSet<>();
        }
    }
}
