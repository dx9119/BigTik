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
import java.util.Arrays;
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
                        @RequestParam(required = false) String searchTags,
                        Model model, Authentication authentication) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Video> videosPage;
        
        List<String> tagList = null;
        if (searchTags != null && !searchTags.isEmpty()) {
            tagList = Arrays.stream(searchTags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        
        boolean hasSearch = (searchTitle != null && !searchTitle.isEmpty()) || dateFrom != null || dateTo != null || (tagList != null && !tagList.isEmpty());
        
        if (hasSearch) {
            videosPage = videoService.searchVideos(searchTitle, dateFrom, dateTo, tagList, pageRequest);
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
        model.addAttribute("searchTags", searchTags);
        model.addAttribute("allTags", videoService.getAllTags());
        return "video/list";
    }

    @PostMapping("/video/{id}/update-title")
    public String updateTitle(@PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam(defaultValue = "0") int page,
                           Authentication authentication, Model model) {
        try {
            videoService.updateVideoTitle(id, title, authentication.getName());
            model.addAttribute("successKey", "video.title.updated");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return listPage(page, 10, null, null, null, null, model, authentication);
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

    @PostMapping("/video/{id}/add-tag")
    public String addTag(@PathVariable Long id,
                       @RequestParam String tag,
                       @RequestParam(defaultValue = "0") int page,
                       Authentication authentication, Model model) {
        try {
            videoService.addTag(id, tag, authentication.getName());
            model.addAttribute("successKey", "video.tag.added");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return listPage(page, 10, null, null, null, null, model, authentication);
    }

    @PostMapping("/video/{id}/remove-tag")
    public String removeTag(@PathVariable Long id,
                        @RequestParam String tag,
                        @RequestParam(defaultValue = "0") int page,
                        Authentication authentication, Model model) {
        try {
            videoService.removeTag(id, tag, authentication.getName());
            model.addAttribute("successKey", "video.tag.removed");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return listPage(page, 10, null, null, null, null, model, authentication);
    }
}