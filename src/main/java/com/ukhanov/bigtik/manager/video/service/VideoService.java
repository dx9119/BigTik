package com.ukhanov.bigtik.manager.video.service;

import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Value("${video.upload.path}")
    private String uploadPath;

    @Value("${video.max.size}")
    private long maxFileSize;

    public VideoService(VideoRepository videoRepository, UserRepository userRepository) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
    }

    public int uploadVideos(String title, List<MultipartFile> files, String tags, String username) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files selected");
        }

        if (files.size() > 1000) {
            throw new IllegalArgumentException("Maximum 1000 files allowed");
        }

        User uploader = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Path userPath = Paths.get(uploadPath, username);

        try {
            Files.createDirectories(userPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to create upload directory: " + e.getMessage());
        }

        int uploadedCount = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            if (file.getSize() > maxFileSize) {
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " exceeds maximum allowed size of 5GB");
            }

            try {
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + extension;
                Path filePath = userPath.resolve(uniqueFilename);
                file.transferTo(filePath.toFile());

                Set<String> tagSet = new HashSet<>();
                if (tags != null && !tags.trim().isEmpty()) {
                    tagSet = new HashSet<>(Arrays.asList(tags.split(",")).stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList());
                }

                String videoTitle = title.isEmpty() ? originalFilename : title;
                Video video = new Video(videoTitle, filePath.toString(), file.getSize(), uploader);
                video.setTags(tagSet);
                videoRepository.save(video);
                uploadedCount++;

            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to save file " + file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        logger.info("Uploaded {} video(s) by user: {}", uploadedCount, username);
        return uploadedCount;
    }

    public Video uploadVideo(String title, MultipartFile file, String tags, String username) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5GB");
        }

        User uploader = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String userFolder = username;
        Path userPath = Paths.get(uploadPath, userFolder);

        try {
            Files.createDirectories(userPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = userPath.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            Set<String> tagSet = new HashSet<>();
            if (tags != null && !tags.trim().isEmpty()) {
                tagSet = new HashSet<>(Arrays.asList(tags.split(",")).stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList());
            }

            Video video = new Video(title, filePath.toString(), file.getSize(), uploader);
            video.setTags(tagSet);
            return videoRepository.save(video);

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save file: " + e.getMessage());
        }
    }

    public void deleteVideo(Long videoId, String username) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.name().equals("ADMIN"));

        boolean isOwner = video.getUploader().getUsername().equals(username);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("You don't have permission to delete this video");
        }

        try {
            Files.deleteIfExists(Paths.get(video.getFilePath()));
        } catch (IOException e) {
            logger.warn("Failed to delete video file: {}", video.getFilePath());
        }

        videoRepository.delete(video);
        logger.info("Deleted video id: {} by user: {}", videoId, username);
    }

    public Video getVideoById(Long videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found"));
    }


    public Page<Video> getAllVideosPaged(Pageable pageable) {
        return videoRepository.findAllByOrderByUploadedAtDesc(pageable);
    }

    public String getUploadSuccessMessage(int count) {
        if (count == 1) {
            return "video.uploaded.1";
        } else if (count <= 4) {
            return "video.uploaded.2_4";
        } else {
            return "video.uploaded.many";
        }
    }
    
    public Page<Video> searchVideos(String title, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return videoRepository.searchVideos(title, dateFrom, dateTo, pageable);
    }

    public Page<Video> getVideosByTags(List<String> tags, Pageable pageable) {
        if (tags.contains("no-tags")) {
            return videoRepository.findByNoTags(pageable);
        }
        return videoRepository.findByTagsIn(tags, pageable);
    }

    public Page<Video> getVideosByNoTags(Pageable pageable) {
        return videoRepository.findByNoTags(pageable);
    }

    public List<String> getAllTags() {
        return videoRepository.findAll().stream()
                .flatMap(v -> v.getTags().stream())
                .distinct()
                .sorted()
                .toList();
    }

    public Video getNextVideo(LocalDateTime afterAt) {
        return videoRepository.findNextVideos(afterAt, PageRequest.of(0, 1)).stream()
                .findFirst()
                .orElse(null);
    }

    public Video getNextVideo(LocalDateTime afterAt, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return getNextVideo(afterAt);
        }
        Page<Video> page = videoRepository.findByTagsIn(tags, 
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        return page.getContent().stream()
                .filter(v -> v.getUploadedAt().isBefore(afterAt))
                .findFirst()
                .orElse(null);
    }

    public Video getNextVideoNoTags(LocalDateTime afterAt) {
        Page<Video> page = videoRepository.findByNoTags(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        return page.getContent().stream()
                .filter(v -> v.getUploadedAt().isBefore(afterAt))
                .findFirst()
                .orElse(null);
    }

    public Video getPreviousVideo(LocalDateTime beforeAt) {
        return videoRepository.findPreviousVideos(beforeAt, PageRequest.of(0, 1)).stream()
                .findFirst()
                .orElse(null);
    }

    public Video getPreviousVideo(LocalDateTime beforeAt, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return getPreviousVideo(beforeAt);
        }
        Page<Video> page = videoRepository.findByTagsIn(tags, 
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        return page.getContent().stream()
                .filter(v -> v.getUploadedAt().isAfter(beforeAt))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public Video getPreviousVideoNoTags(LocalDateTime beforeAt) {
        Page<Video> page = videoRepository.findByNoTags(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        return page.getContent().stream()
                .filter(v -> v.getUploadedAt().isAfter(beforeAt))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public Video getRandomVideo() {
        List<Long> ids = videoRepository.findAllIds();
        if (ids.isEmpty()) return null;

        int randomIndex = (int) (Math.random() * ids.size());
        Long randomId = ids.get(randomIndex);

        return videoRepository.findById(randomId).orElse(null);
    }

    public Video getRandomVideoByTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return getRandomVideo();
        }
        Page<Video> page = videoRepository.findByTagsIn(tags, 
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        List<Video> videos = page.getContent();
        if (videos.isEmpty()) return null;

        int randomIndex = (int) (Math.random() * videos.size());
        return videos.get(randomIndex);
    }

    public Video getRandomVideoNoTags() {
        Page<Video> page = videoRepository.findByNoTags(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        List<Video> videos = page.getContent();
        if (videos.isEmpty()) return null;

        int randomIndex = (int) (Math.random() * videos.size());
        return videos.get(randomIndex);
    }

    public Video addTag(Long videoId, String tag, String username) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.name().equals("ADMIN"));
        boolean isOwner = video.getUploader().getUsername().equals(username);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("You don't have permission to modify tags");
        }

        String trimmedTag = tag.trim();
        if (!trimmedTag.isEmpty()) {
            video.getTags().add(trimmedTag);
            videoRepository.save(video);
        }

        return video;
    }

    public Video removeTag(Long videoId, String tag, String username) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.name().equals("ADMIN"));
        boolean isOwner = video.getUploader().getUsername().equals(username);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("You don't have permission to modify tags");
        }

        video.getTags().remove(tag.trim());
        videoRepository.save(video);

        return video;
    }

}