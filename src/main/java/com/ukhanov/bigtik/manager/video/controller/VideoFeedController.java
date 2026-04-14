package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
public class VideoFeedController {

    private final VideoService videoService;

    public VideoFeedController(VideoService videoService) {
        this.videoService = videoService;
    }

@GetMapping("/video/feed")
    public String feed(@RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "1") int size,
                   Model model, Authentication authentication) {
        Page<Video> videosPage = videoService.getAllVideosPaged(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        
        model.addAttribute("videos", videosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", videosPage.getTotalPages());
        model.addAttribute("totalElements", videosPage.getTotalElements());
        model.addAttribute("currentUsername", authentication.getName());
        
        return "video/feed";
    }
}