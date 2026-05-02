package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class VideoTagController {

    private final VideoService videoService;

    public VideoTagController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/video/tags")
    public String tagsPage(@RequestParam(required = false) String tags,
                          @RequestParam(required = false, defaultValue = "false") boolean noTags,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "12") int size,
                          Model model, Authentication authentication) {
        List<String> allTags = videoService.getAllTags();
        model.addAttribute("allTags", allTags);
        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("videos", List.of());

        if (noTags) {
            Page<Video> videosPage = videoService.getVideosByNoTags(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt")));

            model.addAttribute("videos", videosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", videosPage.getTotalPages());
            model.addAttribute("totalElements", videosPage.getTotalElements());
            model.addAttribute("selectedNoTags", true);
        } else if (tags != null && !tags.isBlank()) {
            List<String> tagList = Arrays.asList(tags.split(",")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (!tagList.isEmpty()) {
                Page<Video> videosPage = videoService.getVideosByTags(tagList,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt")));

                model.addAttribute("videos", videosPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", videosPage.getTotalPages());
                model.addAttribute("totalElements", videosPage.getTotalElements());
                model.addAttribute("selectedTags", tags);
            }
        }

        return "video/tags";
    }
}