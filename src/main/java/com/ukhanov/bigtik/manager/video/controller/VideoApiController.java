package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoApiController {

    private final VideoService videoService;

    public VideoApiController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/next/{id}")
    public ResponseEntity<?> getNextVideo(@PathVariable Long id) {
        try {
            Video current = videoService.getVideoById(id);
            Video next = videoService.getNextVideo(current.getUploadedAt());
            if (next == null) {
                return ResponseEntity.ok(Map.of("hasNext", false));
            }
            // For the returned video, check if it has neighbors
            Video nextNext = videoService.getNextVideo(next.getUploadedAt());
            Video nextPrev = videoService.getPreviousVideo(next.getUploadedAt());
            return ResponseEntity.ok(buildVideoMap(next, nextNext != null, nextPrev != null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/prev/{id}")
    public ResponseEntity<?> getPreviousVideo(@PathVariable Long id) {
        try {
            Video current = videoService.getVideoById(id);
            Video prev = videoService.getPreviousVideo(current.getUploadedAt());
            if (prev == null) {
                return ResponseEntity.ok(Map.of("hasPrev", false));
            }
            // For the returned video, check if it has neighbors
            Video prevNext = videoService.getNextVideo(prev.getUploadedAt());
            Video prevPrev = videoService.getPreviousVideo(prev.getUploadedAt());
            return ResponseEntity.ok(buildVideoMap(prev, prevNext != null, prevPrev != null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> buildVideoMap(Video video, boolean hasNext, boolean hasPrev) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", video.getId());
        map.put("title", video.getTitle());
        map.put("uploader", video.getUploader().getUsername());
        map.put("uploadedAt", video.getUploadedAt());
        map.put("tags", video.getTags());
        map.put("hasNext", hasNext);
        map.put("hasPrev", hasPrev);
        map.put("streamUrl", "/video/play/" + video.getId());
        return map;
    }

    @GetMapping("/bounds/{id}")
    public ResponseEntity<?> getVideoBounds(@PathVariable Long id) {
        try {
            Video current = videoService.getVideoById(id);
            Video next = videoService.getNextVideo(current.getUploadedAt());
            Video prev = videoService.getPreviousVideo(current.getUploadedAt());
            Map<String, Object> map = new HashMap<>();
            map.put("hasNext", next != null);
            map.put("hasPrev", prev != null);
            return ResponseEntity.ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/random")
    public ResponseEntity<?> getRandomVideo() {
        Video video = videoService.getRandomVideo();
        if (video == null) {
            return ResponseEntity.ok(Map.of("error", "No videos available"));
        }
        Video next = videoService.getNextVideo(video.getUploadedAt());
        Video prev = videoService.getPreviousVideo(video.getUploadedAt());
        return ResponseEntity.ok(buildVideoMap(video, next != null, prev != null));
    }
}