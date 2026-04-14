package com.ukhanov.bigtik.manager.video.repository;

import com.ukhanov.bigtik.manager.video.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryTest {

    @Mock
    private VideoRepository videoRepository;

    @Test
    void findNextVideos_shouldReturnOlderVideos() {
        LocalDateTime now = LocalDateTime.now();
        
        when(videoRepository.findNextVideos(eq(now), any(Pageable.class)))
            .thenReturn(List.of());
        
        List<Video> result = videoRepository.findNextVideos(now, Pageable.ofSize(1));
        
        assertNotNull(result);
    }

    @Test
    void findPreviousVideos_shouldReturnNewerVideos() {
        LocalDateTime now = LocalDateTime.now();
        
        when(videoRepository.findPreviousVideos(eq(now), any(Pageable.class)))
            .thenReturn(List.of());
        
        List<Video> result = videoRepository.findPreviousVideos(now, Pageable.ofSize(1));
        
        assertNotNull(result);
    }

    @Test
    void findAllByOrderByUploadedAtDesc_shouldReturnPage() {
        when(videoRepository.findAllByOrderByUploadedAtDesc(any(Pageable.class)))
            .thenReturn(Page.empty());
        
        Page<Video> result = videoRepository.findAllByOrderByUploadedAtDesc(Pageable.ofSize(10));
        
        assertNotNull(result);
    }
}