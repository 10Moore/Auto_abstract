package com.example.auto_abstracts.repository;

import com.example.auto_abstracts.entity.FolderFileRating;
import com.example.auto_abstracts.entity.FolderFileRatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface FolderFileRatingRepository extends JpaRepository<FolderFileRating, FolderFileRatingId> {
    @Query("SELECT r FROM FolderFileRating r WHERE r.folder.id = :folderId")
    List<FolderFileRating> findByFolderId(@Param("folderId") Long folderId);
}
