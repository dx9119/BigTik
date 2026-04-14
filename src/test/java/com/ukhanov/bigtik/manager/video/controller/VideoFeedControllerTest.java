package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoFeedControllerTest {

    @Mock
    private VideoService videoService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private VideoFeedController controller;

    @Test
    void feed_shouldReturnFeedTemplate() {
        when(authentication.getName()).thenReturn("testuser");
        when(videoService.getAllVideosPaged(any())).thenReturn(Page.empty());
        
        String view = controller.feed(0, 1, model, authentication);
        
        assertEquals("video/feed", view);
    }

    @Test
    void feed_shouldAddVideosToModel() {
        when(authentication.getName()).thenReturn("testuser");
        
        Video video = new Video();
        video.setId(1L);
        video.setTitle("Test Video");
        
        Page<Video> videoPage = new PageImpl<>(List.of(video));
        when(videoService.getAllVideosPaged(any())).thenReturn(videoPage);
        
        String view = controller.feed(0, 1, model, authentication);
        
        verify(model).addAttribute("videos", List.of(video));
    }

    @Test
    void feed_shouldAddPaginationAttributes() {
        when(authentication.getName()).thenReturn("testuser");
        
        Page<Video> videoPage = new PageImpl<>(Collections.emptyList(), 
            org.springframework.data.domain.PageRequest.of(0, 1), 10);
        when(videoService.getAllVideosPaged(any())).thenReturn(videoPage);
        
        String view = controller.feed(0, 1, model, authentication);
        
        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute(eq("totalPages"), any());
        verify(model).addAttribute(eq("totalElements"), any());
    }

    @Test
    void feed_shouldOrderByUploadedAtDesc() {
        when(authentication.getName()).thenReturn("testuser");
        when(videoService.getAllVideosPaged(any())).thenReturn(Page.empty());
        
        controller.feed(0, 1, model, authentication);
        
        verify(videoService).getAllVideosPaged(argThat(pageable -> 
            pageable.getSort().getOrderFor("uploadedAt") != null));
    }

    @Test
    void feed_shouldReturnSingleVideo() {
        when(authentication.getName()).thenReturn("testuser");
        when(videoService.getAllVideosPaged(any())).thenReturn(Page.empty());
        
        String view = controller.feed(0, 1, model, authentication);
        
        verify(videoService).getAllVideosPaged(argThat(pageable -> 
            pageable.getPageSize() == 1));
    }
}