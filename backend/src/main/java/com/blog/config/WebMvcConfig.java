package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 判断是否为绝对路径
        String absolutePath;
        java.nio.file.Path path = Paths.get(uploadPath);
        if (path.isAbsolute()) {
            absolutePath = path.toString();
        } else {
            String userDir = System.getProperty("user.dir");
            absolutePath = Paths.get(userDir, uploadPath).toAbsolutePath().toString();
        }
        
        // 配置上传文件的访问路径
        // 优化：添加缓存控制，图片和头像缓存30天
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS)
                        .cachePublic()
                        .mustRevalidate())
                .resourceChain(true);  // 启用资源链，提升性能
    }
}
