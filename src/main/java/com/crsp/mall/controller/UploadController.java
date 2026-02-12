package com.crsp.mall.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
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

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(".mp4", ".webm", ".ogg");
    private static final long MAX_DOWNLOAD_SIZE = 50L * 1024 * 1024; // 50MB
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_REDIRECTS = 5;
    private static final Set<Integer> REDIRECT_STATUS_CODES = Set.of(301, 302, 307, 308);

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

        // 检查文件大小（最大50MB）
        if (file.getSize() > MAX_DOWNLOAD_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件过大，最大支持50MB"));
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
            // 验证文件内容的魔术字节（防止伪造Content-Type）
            byte[] header = new byte[12];
            int bytesRead;
            try (InputStream is = file.getInputStream()) {
                bytesRead = is.read(header);
                if (bytesRead < 4) {
                    return ResponseEntity.badRequest().body(Map.of("error", "文件内容无效"));
                }
            }
            if (!isValidFileContent(header, bytesRead, mediaType)) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件内容与声明的类型不匹配"));
            }

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
            return ResponseEntity.internalServerError().body(Map.of("error", "文件上传失败"));
        }
    }

    /**
     * 从URL下载图片或视频到服务器
     */
    @PostMapping("/upload/from-url")
    public ResponseEntity<?> uploadFromUrl(@RequestBody Map<String, String> request,
                                           HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }

        String sourceUrl = request.get("url");
        if (sourceUrl == null || sourceUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL不能为空"));
        }
        sourceUrl = sourceUrl.trim();

        // 验证URL格式
        if (!sourceUrl.startsWith("http://") && !sourceUrl.startsWith("https://")) {
            return ResponseEntity.badRequest().body(Map.of("error", "仅支持http和https链接"));
        }

        // 解析URL，防止SSRF攻击：禁止访问内网地址
        URI uri;
        try {
            uri = URI.create(sourceUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的URL格式"));
        }
        String host = uri.getHost();
        if (host == null || isPrivateOrReservedHost(host)) {
            return ResponseEntity.badRequest().body(Map.of("error", "不允许访问内网地址"));
        }

        // 从URL路径提取文件名
        String urlPath = uri.getPath();
        if (urlPath == null || urlPath.isEmpty() || urlPath.equals("/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "无法从URL中提取文件名"));
        }
        String originalFilename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        // 去掉查询参数
        if (originalFilename.contains("?")) {
            originalFilename = originalFilename.substring(0, originalFilename.indexOf('?'));
        }

        // 提取扩展名
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // 验证扩展名并确定媒体类型
        String mediaType;
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            mediaType = "image";
        } else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
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

            // 使用UUID前缀+原始文件名以避免冲突，同时保留原始文件名信息
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            // 防止以点或连字符开头
            if (safeFilename.startsWith(".") || safeFilename.startsWith("-")) {
                safeFilename = "_" + safeFilename;
            }
            String filename = UUID.randomUUID().toString() + "_" + safeFilename;
            Path filePath = Paths.get(uploadDir, filename);

            // 下载文件 (disable automatic redirects to prevent SSRF via redirect)
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(DOWNLOAD_TIMEOUT)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(DOWNLOAD_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            // Handle redirects manually with SSRF validation
            int redirectCount = 0;
            URI currentUri = uri;
            while (REDIRECT_STATUS_CODES.contains(response.statusCode())
                    && redirectCount < MAX_REDIRECTS) {
                String location = response.headers().firstValue("Location").orElse(null);
                if (location == null) break;
                URI redirectUri;
                try {
                    redirectUri = currentUri.resolve(location);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "重定向URL无效"));
                }
                String redirectHost = redirectUri.getHost();
                if (redirectHost == null || isPrivateOrReservedHost(redirectHost)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "不允许重定向到内网地址"));
                }
                String redirectScheme = redirectUri.getScheme();
                if (!"http".equals(redirectScheme) && !"https".equals(redirectScheme)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "仅支持http和https链接"));
                }
                currentUri = redirectUri;
                httpRequest = HttpRequest.newBuilder()
                        .uri(redirectUri)
                        .timeout(DOWNLOAD_TIMEOUT)
                        .GET()
                        .build();
                response = client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
                redirectCount++;
            }

            if (response.statusCode() != 200) {
                return ResponseEntity.badRequest().body(Map.of("error", "下载失败，远程服务器返回非成功状态码"));
            }

            // 检查Content-Length防止下载过大文件
            long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            if (contentLength > MAX_DOWNLOAD_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件过大，最大支持50MB"));
            }

            // 使用有界流保存文件，防止超过大小限制
            try (InputStream is = response.body()) {
                long bytesWritten = 0;
                byte[] buffer = new byte[8192];
                try (var out = Files.newOutputStream(filePath)) {
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        bytesWritten += read;
                        if (bytesWritten > MAX_DOWNLOAD_SIZE) {
                            out.close();
                            Files.deleteIfExists(filePath);
                            return ResponseEntity.badRequest().body(Map.of("error", "文件过大，最大支持50MB"));
                        }
                        out.write(buffer, 0, read);
                    }
                }
            }

            // 验证实际文件大小
            long fileSize = Files.size(filePath);
            if (fileSize == 0) {
                Files.deleteIfExists(filePath);
                return ResponseEntity.badRequest().body(Map.of("error", "下载的文件为空"));
            }

            // 返回访问URL
            String localUrl = "/uploads/" + filename;
            return ResponseEntity.ok(Map.of(
                "url", localUrl,
                "type", mediaType,
                "filename", filename
            ));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return ResponseEntity.internalServerError().body(Map.of("error", "下载文件失败"));
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
        // Defense-in-depth: verify resolved path is within upload directory
        try {
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!resolvedPath.startsWith(uploadRoot)) {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的文件路径"));
            }
            Files.deleteIfExists(resolvedPath);
            return ResponseEntity.ok(Map.of("message", "文件删除成功"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件删除失败"));
        }
    }

    /**
     * 检查主机名是否为私有或保留地址
     */
    private static boolean isPrivateOrReservedHost(String host) {
        if (host == null) return true;
        String h = host.toLowerCase().replaceAll("[\\[\\]]", "");
        // 常见内网主机名
        if (h.equals("localhost") || h.equals("0.0.0.0") || h.equals("::1")) {
            return true;
        }
        // IPv4 私有/保留地址
        if (h.startsWith("0.") || h.startsWith("10.") || h.startsWith("192.168.") || h.startsWith("169.254.") || h.startsWith("127.")) {
            return true;
        }
        // 172.16.0.0 - 172.31.255.255
        if (h.startsWith("172.") && isSecondOctetInRange(h, 16, 31)) {
            return true;
        }
        // 100.64.0.0 - 100.127.255.255 (CGNAT / Shared Address Space RFC 6598)
        if (h.startsWith("100.") && isSecondOctetInRange(h, 64, 127)) {
            return true;
        }
        // IPv6 私有地址
        if (h.startsWith("fc") || h.startsWith("fd") || h.startsWith("fe80")) {
            return true;
        }
        // DNS rebinding protection: resolve hostname and check if it resolves to a private IP
        try {
            java.net.InetAddress[] addresses = java.net.InetAddress.getAllByName(h);
            for (java.net.InetAddress addr : addresses) {
                if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()
                        || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                    return true;
                }
                // Check ranges not covered by InetAddress methods (CGNAT, 0.0.0.0/8)
                if (addr instanceof java.net.Inet4Address) {
                    byte[] bytes = addr.getAddress();
                    int firstOctet = bytes[0] & 0xFF;
                    int secondOctet = bytes[1] & 0xFF;
                    // 0.0.0.0/8
                    if (firstOctet == 0) return true;
                    // 100.64.0.0/10 (CGNAT)
                    if (firstOctet == 100 && secondOctet >= 64 && secondOctet <= 127) return true;
                }
            }
        } catch (java.net.UnknownHostException e) {
            // Cannot resolve hostname - block it to be safe
            return true;
        }
        return false;
    }

    /**
     * 检查IPv4地址的第二个八位组是否在指定范围内
     */
    private static boolean isSecondOctetInRange(String host, int min, int max) {
        try {
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                int second = Integer.parseInt(parts[1]);
                return second >= min && second <= max;
            }
        } catch (NumberFormatException ignored) {}
        return false;
    }

    /**
     * 验证文件内容的魔术字节是否匹配声明的媒体类型
     */
    private static boolean isValidFileContent(byte[] header, int bytesRead, String mediaType) {
        if (header == null || bytesRead < 4) return false;
        if ("image".equals(mediaType)) {
            // JPEG: FF D8 FF
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) return true;
            // PNG: 89 50 4E 47
            if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return true;
            // GIF: 47 49 46 38
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) return true;
            // WEBP: RIFF....WEBP (requires 12 bytes)
            if (bytesRead >= 12 && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) return true;
            return false;
        } else if ("video".equals(mediaType)) {
            // MP4/MOV: ....ftyp or ....moov or ....mdat (requires 8 bytes)
            if (bytesRead >= 8) {
                String box = new String(header, 4, 4, java.nio.charset.StandardCharsets.US_ASCII);
                if ("ftyp".equals(box) || "moov".equals(box) || "mdat".equals(box)) return true;
            }
            // WEBM/MKV: 1A 45 DF A3
            if (header[0] == 0x1A && header[1] == 0x45 && header[2] == (byte) 0xDF && header[3] == (byte) 0xA3) return true;
            // OGG: OggS
            if (header[0] == 0x4F && header[1] == 0x67 && header[2] == 0x67 && header[3] == 0x53) return true;
            return false;
        }
        return false;
    }
}
