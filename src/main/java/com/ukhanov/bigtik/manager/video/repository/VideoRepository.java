package com.ukhanov.bigtik.manager.video.repository;

import com.ukhanov.bigtik.manager.video.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findAllByOrderByUploadedAtDesc();
    List<Video> findByUploader_UsernameOrderByUploadedAtDesc(String username);
    Page<Video> findAllByOrderByUploadedAtDesc(Pageable pageable);
    
    @Query("SELECT v FROM Video v WHERE " +
           "(:title IS NULL OR LOWER(v.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:dateFrom IS NULL OR v.uploadedAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR v.uploadedAt <= :dateTo)")
    Page<Video> searchVideos(@Param("title") String title,
                         @Param("dateFrom") LocalDateTime dateFrom,
                         @Param("dateTo") LocalDateTime dateTo,
                         Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.uploadedAt < :uploadedAt ORDER BY v.uploadedAt DESC")
    List<Video> findNextVideos(@Param("uploadedAt") LocalDateTime uploadedAt, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.uploadedAt > :uploadedAt ORDER BY v.uploadedAt ASC")
    List<Video> findPreviousVideos(@Param("uploadedAt") LocalDateTime uploadedAt, Pageable pageable);

    Optional<Video> findById(Long id);

    @Query("SELECT v.id FROM Video v")
    List<Long> findAllIds();

    @Query("SELECT v FROM Video v WHERE :tag MEMBER OF v.tags ORDER BY v.uploadedAt DESC")
    List<Video> findByTagsContaining(@Param("tag") String tag);

    @Query("SELECT v FROM Video v WHERE :tag MEMBER OF v.tags ORDER BY v.uploadedAt DESC")
    Page<Video> findByTagsContaining(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT DISTINCT v FROM Video v JOIN v.tags t WHERE t IN (:tags) ORDER BY v.uploadedAt DESC")
    List<Video> findByTagsIn(@Param("tags") List<String> tags);

    @Query("SELECT DISTINCT v FROM Video v JOIN v.tags t WHERE t IN (:tags) ORDER BY v.uploadedAt DESC")
    Page<Video> findByTagsIn(@Param("tags") List<String> tags, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.tags IS EMPTY ORDER BY v.uploadedAt DESC")
    List<Video> findByNoTags();

    @Query("SELECT v FROM Video v WHERE v.tags IS EMPTY ORDER BY v.uploadedAt DESC")
    Page<Video> findByNoTags(Pageable pageable);



}