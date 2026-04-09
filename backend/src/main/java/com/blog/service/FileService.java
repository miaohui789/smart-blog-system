package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    private static final long JPEG_HIGH_QUALITY_THRESHOLD = 2L * 1024 * 1024;
    private static final int JPEG_MAX_DIMENSION = 2560;
    private static final float JPEG_QUALITY_HIGH = 0.95f;
    private static final float JPEG_QUALITY_LARGE = 0.92f;
    private static final float PNG_COMPRESSION = 0.9f;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    private Path baseUploadPath;

    @PostConstruct
    public void init() {
        // 判断是否为绝对路径
        Path path = Paths.get(uploadPath);
        if (path.isAbsolute()) {
            baseUploadPath = path;
        } else {
            // 相对路径则拼接项目根目录
            String userDir = System.getProperty("user.dir");
            baseUploadPath = Paths.get(userDir, uploadPath).toAbsolutePath();
        }
        log.info("文件上传目录: {}", baseUploadPath);
        
        // 确保目录存在
        try {
            if (!Files.exists(baseUploadPath)) {
                Files.createDirectories(baseUploadPath);
            }
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
        }
    }

    /**
     * 上传文件到本地存储
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
        Path filePath = createTargetPath(extension);
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("文件上传成功: {}", filePath);
        return buildAccessUrl(filePath);
    }

    public String uploadImage(MultipartFile file) throws IOException {
        String normalizedExtension = normalizeImageExtension(file.getOriginalFilename(), file.getContentType());
        if (normalizedExtension == null) {
            return uploadFile(file);
        }

        BufferedImage sourceImage;
        try (var inputStream = file.getInputStream()) {
            sourceImage = ImageIO.read(inputStream);
        }
        if (sourceImage == null) {
            return uploadFile(file);
        }

        BufferedImage outputImage = resizeIfNeeded(sourceImage, normalizedExtension);
        Path filePath = createTargetPath(normalizedExtension);
        writeImage(outputImage, filePath, normalizedExtension, file.getSize());
        log.info("图片上传成功: {} -> {}", file.getOriginalFilename(), filePath);
        return buildAccessUrl(filePath);
    }

    private Path createTargetPath(String extension) throws IOException {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID() + extension;
        Path dirPath = baseUploadPath.resolve(datePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        return dirPath.resolve(fileName);
    }

    private String buildAccessUrl(Path filePath) {
        String relativePath = baseUploadPath.relativize(filePath).toString().replace("\\", "/");
        return "/uploads/" + relativePath;
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }

    private String normalizeImageExtension(String originalFilename, String contentType) {
        String extension = getFileExtension(originalFilename);
        if (".jpg".equals(extension) || ".jpeg".equals(extension) || "image/jpeg".equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if (".png".equals(extension) || "image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return null;
    }

    private BufferedImage resizeIfNeeded(BufferedImage sourceImage, String extension) {
        if (!".jpg".equals(extension)) {
            return sourceImage;
        }
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int maxDimension = Math.max(width, height);
        if (maxDimension <= JPEG_MAX_DIMENSION) {
            return sourceImage;
        }
        double scale = (double) JPEG_MAX_DIMENSION / maxDimension;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return resized;
    }

    private void writeImage(BufferedImage image, Path filePath, String extension, long originalSize) throws IOException {
        String formatName = ".png".equals(extension) ? "png" : "jpg";
        BufferedImage outputImage = ".jpg".equals(extension) ? toJpegImage(image) : image;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (!writers.hasNext()) {
            ImageIO.write(outputImage, formatName, filePath.toFile());
            return;
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(Files.newOutputStream(filePath))) {
            writer.setOutput(outputStream);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (".jpg".equals(extension)) {
                    writeParam.setCompressionQuality(originalSize > JPEG_HIGH_QUALITY_THRESHOLD ? JPEG_QUALITY_LARGE : JPEG_QUALITY_HIGH);
                } else {
                    writeParam.setCompressionQuality(PNG_COMPRESSION);
                }
            }
            writer.write(null, new IIOImage(outputImage, null, null), writeParam);
        } finally {
            writer.dispose();
        }
    }

    private BufferedImage toJpegImage(BufferedImage sourceImage) {
        if (sourceImage.getType() == BufferedImage.TYPE_INT_RGB) {
            return sourceImage;
        }
        BufferedImage rgbImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgbImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(sourceImage, 0, 0, null);
        graphics.dispose();
        return rgbImage;
    }
}
