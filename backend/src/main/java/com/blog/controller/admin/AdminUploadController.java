package com.blog.controller.admin;

import com.blog.common.enums.ResultCode;
import com.blog.common.result.Result;
import com.blog.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "管理端文件上传")
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final FileService fileService;

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(ResultCode.PARAM_ERROR, "只能上传图片文件");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error(ResultCode.PARAM_ERROR, "图片大小不能超过10MB");
        }

        try {
            String url = fileService.uploadImage(file);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "上传文件")
    @PostMapping("/file")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "请选择要上传的文件");
        }

        if (file.getSize() > 20 * 1024 * 1024) {
            return Result.error(ResultCode.PARAM_ERROR, "文件大小不能超过20MB");
        }

        try {
            String url = fileService.uploadFile(file);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}
