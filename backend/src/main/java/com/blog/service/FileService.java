package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

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
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 按日期分目录存储
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 创建目录 - 使用绝对路径
        Path dirPath = baseUploadPath.resolve(datePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        // 保存文件 - 使用绝对路径
        Path filePath = dirPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        log.info("文件上传成功: {}", filePath);
        
        // 返回访问路径
        return "/uploads/" + datePath + "/" + fileName;
    }
}
