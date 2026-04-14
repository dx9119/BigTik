package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

@Controller
public class VideoStreamController {

    private final VideoService videoService;
    private final UserRepository userRepository;

    @Value("${video.upload.path}")
    private String uploadPath;

    public VideoStreamController(VideoService videoService, UserRepository userRepository) {
        this.videoService = videoService;
        this.userRepository = userRepository;
    }

    @GetMapping("/video/play/{id}")
    @ResponseBody
    public Resource streamVideo(@PathVariable Long id,
                               @RequestHeader(value = "Range", required = false) String range,
                               HttpServletResponse response) throws MalformedURLException, IOException {
        Video video = videoService.getVideoById(id);
        Path videoPath = Paths.get(video.getFilePath());
        Resource resource = new UrlResource(videoPath.toUri());
        
        String contentType = "video/mp4";
        response.setContentType(contentType);
        response.setContentLengthLong(video.getFileSize());
        response.setHeader("Content-Disposition", "inline; filename=\"" + video.getTitle() + "\"");
        response.setHeader("Accept-Ranges", "bytes");
        
        return resource;
    }
}