package com.ukhanov.bigtik.manager.image;

import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public String listImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) Set<String> tags,
            Model model,
            Principal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<Image> images;

        if (tags != null && !tags.isEmpty()) {
            images = imageService.getImagesByTags(tags, pageable);
        } else {
            images = imageService.getAllImages(pageable);
        }

        model.addAttribute("images", images);
        model.addAttribute("tags", tags);
        model.addAttribute("currentUser", principal != null ? principal.getName() : null);

        return "images/list";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "images/upload";
    }

    @PostMapping("/upload")
    public String uploadImages(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "tags", required = false) String tags,
            Principal principal,
            Model model) {

        if (principal == null) {
            model.addAttribute("error", "User not authenticated");
            return "images/upload";
        }

        String username = principal.getName();

        try {
            int count = imageService.uploadImages(title, files, tags, username);
            model.addAttribute("message", "Uploaded " + count + " images");
            return "redirect:/images";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "images/upload";
        }
    }

    @GetMapping("/{id}")
    public String viewImage(@PathVariable Long id, Model model, Principal principal) {
        Image image = imageService.getImageById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        model.addAttribute("image", image);
        model.addAttribute("currentUser", principal != null ? principal.getName() : null);

        return "images/view";
    }

    @PostMapping("/{id}/delete")
    public String deleteImage(@PathVariable Long id, Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("error", "User not authenticated");
            return "redirect:/images";
        }

        try {
            imageService.deleteImage(id, principal.getName());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/images";
    }

    @PostMapping("/{id}/tags/add")
    public String addTag(
            @PathVariable Long id,
            @RequestParam String tag,
            Principal principal,
            Model model) {

        if (principal == null) {
            model.addAttribute("error", "User not authenticated");
            return "redirect:/images/" + id;
        }

        try {
            imageService.addTag(id, tag, principal.getName());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/images/" + id;
    }

    @PostMapping("/{id}/tags/remove")
    public String removeTag(
            @PathVariable Long id,
            @RequestParam String tag,
            Principal principal,
            Model model) {

        if (principal == null) {
            model.addAttribute("error", "User not authenticated");
            return "redirect:/images/" + id;
        }

        try {
            imageService.removeTag(id, tag, principal.getName());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/images/" + id;
    }

    @GetMapping("/{id}/content")
    @ResponseBody
    public ResponseEntity<byte[]> getImageContent(@PathVariable Long id) throws IOException {
        Image image = imageService.getImageById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        // Читаем файл с диска по пути filePath
        Path imagePath = Paths.get(image.getFilePath());
        byte[] imageData = Files.readAllBytes(imagePath);

        // Определяем content type
        String contentType = Files.probeContentType(imagePath);
        if (contentType == null) {
            contentType = "image/jpeg"; // значение по умолчанию
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageData);
    }

    @PostMapping("/check-hash")
    @ResponseBody
    public Map<String, Boolean> checkHashExists(@RequestBody Map<String, String> request) {
        String hash = request.get("hash");
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", imageService.existsByHash(hash));
        return response;
    }

    @PostMapping("/check-hashes")
    @ResponseBody
    public Map<String, List<String>> checkHashesExist(@RequestBody Map<String, List<String>> request) {
        List<String> hashes = request.get("hashes");
        List<String> existing = imageService.findExistingHashes(new HashSet<>(hashes));

        Map<String, List<String>> response = new HashMap<>();
        response.put("existing", existing);
        return response;
    }

    @PostMapping("/upload-batch")
    @ResponseBody
    public Map<String, Object> uploadImagesBatch(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "tags", required = false) String tags,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("error", "User not authenticated");
            return response;
        }

        try {
            Map<String, Object> result = imageService.uploadImagesWithResult(title, files, tags, principal.getName());
            response.putAll(result);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/upload-single")
    @ResponseBody
    public Map<String, Object> uploadSingleImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) String tags,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("error", "User not authenticated");
            return response;
        }

        if (file.isEmpty()) {
            response.put("error", "File is empty");
            return response;
        }

        try {
            Map<String, Object> result = imageService.uploadSingleImage(file, title, tags, principal.getName());
            response.putAll(result);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }
}