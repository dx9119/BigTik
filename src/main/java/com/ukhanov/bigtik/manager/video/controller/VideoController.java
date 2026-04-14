package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/video/upload")
    public String uploadPage() {
        return "video/upload";
    }

@PostMapping("/video/upload")
    public String upload(@RequestParam String title,
                     @RequestParam List<MultipartFile> files,
                     @RequestParam(required = false) String tags,
                     Authentication authentication,
                     Model model) {
        try {
            int uploadedCount = videoService.uploadVideos(title, files, tags, authentication.getName());
            model.addAttribute("successKey", videoService.getUploadSuccessMessage(uploadedCount));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "video/upload";
    }

    @GetMapping("/video/list")
    public String listPage(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String searchTitle,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
                        Model model, Authentication authentication) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Video> videosPage;
        
        if (searchTitle != null || dateFrom != null || dateTo != null) {
            videosPage = videoService.searchVideos(searchTitle, dateFrom, dateTo, pageRequest);
        } else {
            videosPage = videoService.getAllVideosPaged(pageRequest);
        }
        
        model.addAttribute("videos", videosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", videosPage.getTotalPages());
        model.addAttribute("totalElements", videosPage.getTotalElements());
        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("searchTitle", searchTitle);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "video/list";
    }

    @PostMapping("/video/{id}/delete")
    public String delete(@PathVariable Long id,
                     @RequestParam(defaultValue = "0") int page,
                     Authentication authentication, Model model) {
        try {
            videoService.deleteVideo(id, authentication.getName());
            model.addAttribute("successKey", "video.deleted");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        Page<Video> videosPage = videoService.getAllVideosPaged(PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        model.addAttribute("videos", videosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", videosPage.getTotalPages());
        model.addAttribute("totalElements", videosPage.getTotalElements());
        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        return "video/list";
    }
}