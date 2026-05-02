package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoApiControllerTest {

    @Mock
    private VideoService videoService;

    private VideoApiController controller;
    private Video currentVideo;
    private User testUser;

    @BeforeEach
    void setUp() {
        controller = new VideoApiController(videoService);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        currentVideo = new Video();
        currentVideo.setId(1L);
        currentVideo.setTitle("Test Video");
        currentVideo.setUploadedAt(LocalDateTime.now());
        currentVideo.setUploader(testUser);
    }



    @Test
    void getNextVideo_whenNoNextVideo_shouldReturnFalse() {
        when(videoService.getVideoById(1L)).thenReturn(currentVideo);
        when(videoService.getNextVideo(any(LocalDateTime.class), any())).thenReturn(null);
        
        ResponseEntity<?> response = controller.getNextVideo(1L, null, false);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("hasNext"));
    }

    @Test
    void getPreviousVideo_whenVideoExists_shouldReturnPreviousVideo() {
        Video prevVideo = new Video();
        prevVideo.setId(2L);
        prevVideo.setTitle("Previous Video");
        prevVideo.setUploadedAt(LocalDateTime.now().plusHours(1));
        prevVideo.setUploader(testUser);
        
        when(videoService.getVideoById(1L)).thenReturn(currentVideo);
        when(videoService.getPreviousVideo(any(LocalDateTime.class), any())).thenReturn(prevVideo);
        when(videoService.getNextVideo(eq(prevVideo.getUploadedAt()), any())).thenReturn(currentVideo);
        when(videoService.getPreviousVideo(eq(prevVideo.getUploadedAt()), any())).thenReturn(null);
        
        ResponseEntity<?> response = controller.getPreviousVideo(1L, null, false);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(2L, body.get("id"));
        assertEquals("Previous Video", body.get("title"));
    }

    @Test
    void getPreviousVideo_whenNoPreviousVideo_shouldReturnFalse() {
        when(videoService.getVideoById(1L)).thenReturn(currentVideo);
        when(videoService.getPreviousVideo(any(LocalDateTime.class), any())).thenReturn(null);
        
        ResponseEntity<?> response = controller.getPreviousVideo(1L, null, false);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("hasPrev"));
    }

    @Test
    void getNextVideo_whenVideoNotFound_shouldReturn404() {
        when(videoService.getVideoById(999L))
            .thenThrow(new IllegalArgumentException("Video not found"));
        
        ResponseEntity<?> response = controller.getNextVideo(999L, null, false);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getPreviousVideo_whenVideoNotFound_shouldReturn404() {
        when(videoService.getVideoById(999L))
            .thenThrow(new IllegalArgumentException("Video not found"));
        
        ResponseEntity<?> response = controller.getPreviousVideo(999L, null, false);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getVideoBounds_shouldReturnBothDirections() {
        Video nextVideo = new Video();
        nextVideo.setId(2L);
        nextVideo.setUploadedAt(LocalDateTime.now().minusHours(1));
        
        when(videoService.getVideoById(1L)).thenReturn(currentVideo);
        when(videoService.getNextVideo(any(LocalDateTime.class), any())).thenReturn(nextVideo);
        when(videoService.getPreviousVideo(any(LocalDateTime.class), any())).thenReturn(null);
        
        ResponseEntity<?> response = controller.getVideoBounds(1L, null, false);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("hasNext"));
        assertFalse((Boolean) body.get("hasPrev"));
    }
}