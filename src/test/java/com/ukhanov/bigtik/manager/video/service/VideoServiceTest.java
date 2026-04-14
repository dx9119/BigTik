package com.ukhanov.bigtik.manager.video.service;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    private VideoService videoService;

    @BeforeEach
    void setUp() {
        videoService = new VideoService(videoRepository, null);
    }

    @Test
    void getNextVideo_shouldReturnOlderVideo() {
        LocalDateTime now = LocalDateTime.now();
        Video nextVideo = new Video();
        nextVideo.setId(2L);
        
        when(videoRepository.findNextVideos(eq(now), any(PageRequest.class)))
            .thenReturn(List.of(nextVideo));
        
        Video result = videoService.getNextVideo(now);
        
        assertNotNull(result);
        assertEquals(2L, result.getId());
    }

    @Test
    void getNextVideo_whenNoVideo_shouldReturnNull() {
        LocalDateTime now = LocalDateTime.now();
        
        when(videoRepository.findNextVideos(eq(now), any(PageRequest.class)))
            .thenReturn(List.of());
        
        Video result = videoService.getNextVideo(now);
        
        assertNull(result);
    }

    @Test
    void getPreviousVideo_shouldReturnNewerVideo() {
        LocalDateTime now = LocalDateTime.now();
        Video prevVideo = new Video();
        prevVideo.setId(2L);
        
        when(videoRepository.findPreviousVideos(eq(now), any(PageRequest.class)))
            .thenReturn(List.of(prevVideo));
        
        Video result = videoService.getPreviousVideo(now);
        
        assertNotNull(result);
        assertEquals(2L, result.getId());
    }

    @Test
    void getPreviousVideo_whenNoVideo_shouldReturnNull() {
        LocalDateTime now = LocalDateTime.now();
        
        when(videoRepository.findPreviousVideos(eq(now), any(PageRequest.class)))
            .thenReturn(List.of());
        
        Video result = videoService.getPreviousVideo(now);
        
        assertNull(result);
    }

    @Test
    void getVideoById_shouldReturnVideo() {
        Video video = new Video();
        video.setId(1L);
        
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        
        Video result = videoService.getVideoById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getVideoById_whenNotFound_shouldThrow() {
        when(videoRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> 
            videoService.getVideoById(999L));
    }

    @Test
    void getAllVideosPaged_shouldReturnPage() {
        Page<Video> videoPage = Page.empty();
        when(videoRepository.findAllByOrderByUploadedAtDesc(any(PageRequest.class)))
            .thenReturn(videoPage);
        
        Page<Video> result = videoService.getAllVideosPaged(PageRequest.of(0, 10));
        
        assertNotNull(result);
    }
}