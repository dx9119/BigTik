package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/video")
public class VideoApiController {

    private final VideoService videoService;

    public VideoApiController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/next/{id}")
    public ResponseEntity<?> getNextVideo(@PathVariable Long id, 
            @RequestParam(required = false) String tags,
            @RequestParam(required = false, defaultValue = "false") boolean noTags) {
        try {
            List<String> tagList = parseTags(tags);
            Video current = videoService.getVideoById(id);
            Video next = noTags 
                    ? videoService.getNextVideoNoTags(current.getUploadedAt())
                    : videoService.getNextVideo(current.getUploadedAt(), tagList);
            if (next == null) {
                return ResponseEntity.ok(Map.of("hasNext", false));
            }
            Video nextNext = noTags 
                    ? videoService.getNextVideoNoTags(next.getUploadedAt())
                    : videoService.getNextVideo(next.getUploadedAt(), tagList);
            Video nextPrev = noTags 
                    ? videoService.getPreviousVideoNoTags(next.getUploadedAt())
                    : videoService.getPreviousVideo(next.getUploadedAt(), tagList);
            return ResponseEntity.ok(buildVideoMap(next, nextNext != null, nextPrev != null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/prev/{id}")
    public ResponseEntity<?> getPreviousVideo(@PathVariable Long id,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false, defaultValue = "false") boolean noTags) {
        try {
            List<String> tagList = parseTags(tags);
            Video current = videoService.getVideoById(id);
            Video prev = noTags 
                    ? videoService.getPreviousVideoNoTags(current.getUploadedAt())
                    : videoService.getPreviousVideo(current.getUploadedAt(), tagList);
            if (prev == null) {
                return ResponseEntity.ok(Map.of("hasPrev", false));
            }
            Video prevNext = noTags 
                    ? videoService.getNextVideoNoTags(prev.getUploadedAt())
                    : videoService.getNextVideo(prev.getUploadedAt(), tagList);
            Video prevPrev = noTags 
                    ? videoService.getPreviousVideoNoTags(prev.getUploadedAt())
                    : videoService.getPreviousVideo(prev.getUploadedAt(), tagList);
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
    public ResponseEntity<?> getVideoBounds(@PathVariable Long id,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false, defaultValue = "false") boolean noTags) {
        try {
            List<String> tagList = parseTags(tags);
            Video current = videoService.getVideoById(id);
            Video next = noTags 
                    ? videoService.getNextVideoNoTags(current.getUploadedAt())
                    : videoService.getNextVideo(current.getUploadedAt(), tagList);
            Video prev = noTags 
                    ? videoService.getPreviousVideoNoTags(current.getUploadedAt())
                    : videoService.getPreviousVideo(current.getUploadedAt(), tagList);
            Map<String, Object> map = new HashMap<>();
            map.put("hasNext", next != null);
            map.put("hasPrev", prev != null);
            return ResponseEntity.ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/random")
    public ResponseEntity<?> getRandomVideo(@RequestParam(required = false) String tags,
            @RequestParam(required = false, defaultValue = "false") boolean noTags) {
        List<String> tagList = parseTags(tags);
        Video video;
        if (noTags) {
            video = videoService.getRandomVideoNoTags();
        } else if (tagList.isEmpty()) {
            video = videoService.getRandomVideo();
        } else {
            video = videoService.getRandomVideoByTags(tagList);
        }
        if (video == null) {
            return ResponseEntity.ok(Map.of("error", "No videos available"));
        }
        Video next = noTags 
                ? videoService.getNextVideoNoTags(video.getUploadedAt())
                : videoService.getNextVideo(video.getUploadedAt(), tagList);
        Video prev = noTags 
                ? videoService.getPreviousVideoNoTags(video.getUploadedAt())
                : videoService.getPreviousVideo(video.getUploadedAt(), tagList);
        return ResponseEntity.ok(buildVideoMap(video, next != null, prev != null));
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}