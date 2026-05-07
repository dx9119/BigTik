package com.ukhanov.bigtik.manager.image;

import com.ukhanov.bigtik.core.model.User;
import com.ukhanov.bigtik.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Value("${images.upload.path}")
    private String uploadPath;

    @Value("${image.max.size:524288000}")
    private long maxFileSize;

    public ImageService(ImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    // -----------------------------
    // GET IMAGE BY ID
    // -----------------------------
    public Optional<Image> getImageById(Long id) {
        return imageRepository.findById(id);
    }

    // -----------------------------
    // UPLOAD MULTIPLE IMAGES
    // -----------------------------
    public int uploadImages(String title, List<MultipartFile> files, String tags, String username) {

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

        Set<String> tagSet = parseTags(tags);
        int uploadedCount = 0;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            if (file.getSize() > maxFileSize) {
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " exceeds max size");
            }

            try {
                String savedPath = saveFile(file, userPath);

                String imageHash = computeHash(Paths.get(savedPath));

                if (imageRepository.existsByHash(imageHash)) {
                    Files.deleteIfExists(Paths.get(savedPath));
                    continue;
                }

                String imageTitle = title == null || title.isEmpty()
                        ? file.getOriginalFilename()
                        : title;

                Image image = new Image(imageTitle, savedPath, file.getSize(), imageHash, uploader);
                image.setTags(tagSet);

                imageRepository.save(image);
                uploadedCount++;

            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to save file " + file.getOriginalFilename());
            }
        }

        logger.info("Uploaded {} image(s) by user {}", uploadedCount, username);
        return uploadedCount;
    }

    // -----------------------------
    // DELETE IMAGE
    // -----------------------------
    public void deleteImage(Long id, String username) {

        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN"));
        boolean isOwner = image.getUploader().getUsername().equals(username);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("You don't have permission to delete this image");
        }

        try {
            Files.deleteIfExists(Paths.get(image.getFilePath()));
        } catch (IOException e) {
            logger.warn("Failed to delete image file {}", image.getFilePath());
        }

        imageRepository.delete(image);
        logger.info("Deleted image {} by {}", id, username);
    }

    // -----------------------------
    // PAGINATION
    // -----------------------------
    public Page<Image> getAllImages(Pageable pageable) {
        return imageRepository.findAll(pageable);
    }

    public Page<Image> getImagesByUploader(User uploader, Pageable pageable) {
        return imageRepository.findByUploader(uploader, pageable);
    }

    public Page<Image> getImagesByTags(Set<String> tags, Pageable pageable) {
        if (tags == null || tags.isEmpty()) {
            return Page.empty(pageable);
        }
        return imageRepository.findByTagsIn(tags, pageable);
    }

    // -----------------------------
    // TAG MANAGEMENT
    // -----------------------------
    public Image addTag(Long id, String tag, String username) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        validatePermission(image, username);

        String trimmed = tag.trim();
        if (!trimmed.isEmpty()) {
            image.getTags().add(trimmed);
            imageRepository.save(image);
        }

        return image;
    }

    public Image removeTag(Long id, String tag, String username) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        validatePermission(image, username);

        image.getTags().remove(tag.trim());
        return imageRepository.save(image);
    }

    // -----------------------------
    // HELPERS
    // -----------------------------
    private Set<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) return new HashSet<>();

        return new HashSet<>(
                Arrays.stream(tags.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
        );
    }

    private String saveFile(MultipartFile file, Path userPath) throws IOException {
        String original = file.getOriginalFilename();
        String ext = "";

        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }

        String unique = UUID.randomUUID() + ext;
        Path filePath = userPath.resolve(unique);

        file.transferTo(filePath.toFile());
        return filePath.toString();
    }

    private String computeHash(Path filePath) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 algorithm not available", e);
        }
    }

    public boolean existsByHash(String hash) {
        return imageRepository.existsByHash(hash);
    }

    public List<String> findExistingHashes(Set<String> hashes) {
        List<String> existing = new ArrayList<>();
        for (String hash : hashes) {
            if (imageRepository.existsByHash(hash)) {
                existing.add(hash);
            }
        }
        return existing;
    }

    public Map<String, Object> uploadImagesWithResult(String title, List<MultipartFile> files, String tags, String username) {
        Map<String, Object> result = new HashMap<>();
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            result.put("error", "No files selected");
            return result;
        }

        if (files.size() > 1000) {
            result.put("error", "Maximum 1000 files allowed");
            return result;
        }

        User uploader = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Path userPath = Paths.get(uploadPath, username);

        try {
            Files.createDirectories(userPath);
        } catch (IOException e) {
            result.put("error", "Failed to create upload directory: " + e.getMessage());
            return result;
        }

        Set<String> tagSet = parseTags(tags);

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                failed.add(file.getOriginalFilename() + " (empty)");
                continue;
            }

            if (file.getSize() > maxFileSize) {
                failed.add(file.getOriginalFilename() + " (exceeds max size)");
                continue;
            }

            try {
                String savedPath = saveFile(file, userPath);

                String imageHash = computeHash(Paths.get(savedPath));

                if (imageRepository.existsByHash(imageHash)) {
                    failed.add(file.getOriginalFilename() + " (duplicate)");
                    Files.deleteIfExists(Paths.get(savedPath));
                    continue;
                }

                String imageTitle = title == null || title.isEmpty()
                        ? file.getOriginalFilename()
                        : title;

                Image image = new Image(imageTitle, savedPath, file.getSize(), imageHash, uploader);
                image.setTags(tagSet);

                imageRepository.save(image);
                successful.add(file.getOriginalFilename());

            } catch (IOException e) {
                failed.add(file.getOriginalFilename() + " (save error)");
            }
        }

        result.put("successful", successful);
        result.put("failed", failed);
        result.put("uploadedCount", successful.size());
        logger.info("Uploaded {} image(s) by user {}, {} failed", successful.size(), username, failed.size());

        return result;
    }

    public Map<String, Object> uploadSingleImage(MultipartFile file, String title, String tags, String username) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "File is empty");
            return result;
        }

        if (file.getSize() > maxFileSize) {
            result.put("error", "File exceeds max size");
            return result;
        }

        User uploader = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Path userPath = Paths.get(uploadPath, username);

        try {
            Files.createDirectories(userPath);
        } catch (IOException e) {
            result.put("error", "Failed to create upload directory: " + e.getMessage());
            return result;
        }

        try {
            String savedPath = saveFile(file, userPath);
            String imageHash = computeHash(Paths.get(savedPath));

            if (imageRepository.existsByHash(imageHash)) {
                Files.deleteIfExists(Paths.get(savedPath));
                result.put("error", "duplicate");
                result.put("filename", file.getOriginalFilename());
                return result;
            }

            Set<String> tagSet = parseTags(tags);
            String imageTitle = title == null || title.isEmpty()
                    ? file.getOriginalFilename()
                    : title;

            Image image = new Image(imageTitle, savedPath, file.getSize(), imageHash, uploader);
            image.setTags(tagSet);

            imageRepository.save(image);
            result.put("success", true);
            result.put("filename", file.getOriginalFilename());

        } catch (IOException e) {
            result.put("error", "Save error: " + e.getMessage());
        }

        return result;
    }

    private void validatePermission(Image image, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN"));
        boolean isOwner = image.getUploader().getUsername().equals(username);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("You don't have permission to modify this image");
        }
    }
}