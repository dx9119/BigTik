package com.ukhanov.bigtik.manager.video.repository;

import com.ukhanov.bigtik.manager.video.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findAllByOrderByUploadedAtDesc();
    List<Video> findByUploader_UsernameOrderByUploadedAtDesc(String username);
    Page<Video> findAllByOrderByUploadedAtDesc(Pageable pageable);
}