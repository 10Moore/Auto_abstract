package com.example.auto_abstracts.entity;

import java.io.Serializable;
import java.util.Objects;

public class FolderFileRatingId implements Serializable {
    private Long folder;
    private Long file;

    // 必须有无参构造函数
    public FolderFileRatingId() {}

    public FolderFileRatingId(Long folder, Long file) {
        this.folder = folder;
        this.file = file;
    }

    // 必须重写equals和hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderFileRatingId that = (FolderFileRatingId) o;
        return Objects.equals(folder, that.folder) &&
                Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folder, file);
    }

    // Getters and Setters
    public Long getFolder() {
        return folder;
    }

    public void setFolder(Long folder) {
        this.folder = folder;
    }

    public Long getFile() {
        return file;
    }

    public void setFile(Long file) {
        this.file = file;
    }


}