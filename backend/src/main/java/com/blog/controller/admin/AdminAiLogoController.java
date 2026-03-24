package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.entity.AiLogo;
import com.blog.service.AiLogoService;
import com.blog.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Tag(name = "管理端-AI Logo管理")
@RestController
@RequestMapping("/api/admin/ai/logo")
@RequiredArgsConstructor
public class AdminAiLogoController {

    private final AiLogoService aiLogoService;
    private final FileService fileService;

    @Operation(summary = "获取所有AI Logo列表")
    @GetMapping("/list")
    public Result<?> getAllLogos() {
        List<AiLogo> logos = aiLogoService.getAllLogos();
        return Result.success(logos);
    }

    @Operation(summary = "获取单个AI Logo")
    @GetMapping("/{id}")
    public Result<?> getLogoById(@PathVariable Long id) {
        AiLogo logo = aiLogoService.getLogoById(id);
        return Result.success(logo);
    }

    @Operation(summary = "新增AI Logo（JSON方式，使用网络地址）")
    @PostMapping
    public Result<?> addLogo(@RequestBody AiLogo logo) {
        if (logo.getProvider() == null || logo.getProvider().isEmpty()) {
            return Result.error("服务商标识不能为空");
        }
        if (logo.getLogoUrl() == null || logo.getLogoUrl().isEmpty()) {
            return Result.error("Logo地址不能为空");
        }
        if (logo.getLogoType() == null) {
            logo.setLogoType("url");
        }
        aiLogoService.addLogo(logo);
        return Result.success();
    }

    @Operation(summary = "上传Logo图片")
    @PostMapping("/upload")
    public Result<?> uploadLogo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("provider") String provider,
            @RequestParam(value = "name", required = false) String name) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        try {
            String url = fileService.uploadFile(file);
            AiLogo logo = new AiLogo();
            logo.setProvider(provider);
            logo.setName(name != null ? name : provider + " Logo");
            logo.setLogoType("upload");
            logo.setLogoUrl(url);
            logo.setSortOrder(0);
            aiLogoService.addLogo(logo);
            return Result.success(logo);
        } catch (Exception e) {
            log.error("Logo上传失败", e);
            return Result.error("Logo上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新AI Logo")
    @PutMapping
    public Result<?> updateLogo(@RequestBody AiLogo logo) {
        if (logo.getId() == null) {
            return Result.error("Logo ID不能为空");
        }
        aiLogoService.updateLogo(logo);
        return Result.success();
    }

    @Operation(summary = "删除AI Logo")
    @DeleteMapping("/{id}")
    public Result<?> deleteLogo(@PathVariable Long id) {
        aiLogoService.deleteLogo(id);
        return Result.success();
    }
}
