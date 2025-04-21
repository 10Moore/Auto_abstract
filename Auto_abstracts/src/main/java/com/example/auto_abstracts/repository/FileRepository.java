package com.example.auto_abstracts.repository;

import com.example.auto_abstracts.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByFilename(String filename);

    @Query("SELECT f FROM FileEntity f JOIN f.folders folder WHERE folder.id = :folderId")
    List<FileEntity> findAllByFolderId(@Param("folderId") Long folderId);
}