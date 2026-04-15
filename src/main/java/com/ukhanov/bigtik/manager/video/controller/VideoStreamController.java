package com.ukhanov.bigtik.manager.video.controller;

import com.ukhanov.bigtik.manager.video.model.Video;
import com.ukhanov.bigtik.manager.video.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class VideoStreamController {

    private static final int BUFFER_SIZE = 8192;

    private final VideoService videoService;

    public VideoStreamController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/video/play/{id}")
    public void streamVideo(@PathVariable Long id,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        Video video = videoService.getVideoById(id);
        Path videoPath = Paths.get(video.getFilePath());

        long fileLength = video.getFileSize();
        String mimeType = resolveMimeType(video.getFilePath());

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(videoPath.toFile(), "r")) {
            long start = 0;
            long end = fileLength - 1;

            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] parts = rangeHeader.substring(6).split("-");
                start = Long.parseLong(parts[0]);
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    end = Long.parseLong(parts[1]);
                }
            }

            long contentLength = end - start + 1;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.setHeader("Accept-Ranges", "bytes");
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Accept-Ranges", "bytes");
            }

            response.setContentType(mimeType);
            response.setContentLengthLong(contentLength);
            response.setHeader("Content-Disposition", "inline; filename=\"" + video.getTitle() + "\"");

            randomAccessFile.seek(start);
            try (OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                long remaining = contentLength;

                while (remaining > 0) {
                    int bytesToRead = (int) Math.min(buffer.length, remaining);
                    int bytesRead = randomAccessFile.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
                out.flush();
            }
        }
    }

    private String resolveMimeType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogg")) return "video/ogg";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        return "application/octet-stream";
    }
}