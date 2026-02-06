package com.crsp.mall.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/admin")
public class UploadController {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
        "video/mp4", "video/webm", "video/ogg"
    );

    @Value("${app.upload.dir:/data/uploads}")
    private String uploadDir;

    /**
     * 上传文件（图片或视频）
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请选择文件"));
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "无法识别文件类型"));
        }

        String mediaType;
        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            mediaType = "image";
        } else if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
            mediaType = "video";
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "不支持的文件类型，仅支持JPG/PNG/GIF/WEBP图片和MP4/WEBM/OGG视频"));
        }

        try {
            // 确保上传目录存在
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                return ResponseEntity.internalServerError().body(Map.of("error", "无法创建上传目录"));
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            // 验证文件扩展名
            Set<String> allowedExtensions = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".mp4", ".webm", ".ogg");
            if (!allowedExtensions.contains(extension)) {
                return ResponseEntity.badRequest().body(Map.of("error", "不支持的文件扩展名"));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件（流式写入避免内存溢出）
            Path filePath = Paths.get(uploadDir, filename);
            file.transferTo(filePath.toFile());

            // 返回访问URL
            String url = "/uploads/" + filename;
            return ResponseEntity.ok(Map.of(
                "url", url,
                "type", mediaType,
                "filename", filename
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 删除已上传的文件
     */
    @PostMapping("/upload/delete")
    public ResponseEntity<?> deleteFile(@RequestBody Map<String, String> request,
                                        HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL不能为空"));
        }

        // 仅允许删除本地上传的文件
        if (!url.startsWith("/uploads/")) {
            return ResponseEntity.ok(Map.of("message", "非本地文件，无需删除"));
        }

        String filename = url.substring("/uploads/".length());
        // 防止路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的文件名"));
        }

        Path filePath = Paths.get(uploadDir, filename);
        try {
            Files.deleteIfExists(filePath);
            return ResponseEntity.ok(Map.of("message", "文件删除成功"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件删除失败"));
        }
    }
}
