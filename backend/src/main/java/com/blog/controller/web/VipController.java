package com.blog.controller.web;

import com.blog.common.result.Result;
import com.blog.dto.request.VipActivateRequest;
import com.blog.entity.Article;
import com.blog.entity.VipMember;
import com.blog.security.SecurityUser;
import com.blog.service.ArticleService;
import com.blog.service.VipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "VIP接口")
@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipService vipService;
    private final ArticleService articleService;

    @Operation(summary = "激活VIP")
    @PostMapping("/activate")
    public Result<?> activate(@Validated @RequestBody VipActivateRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        try {
            VipMember member = vipService.activate(userId, request);
            return Result.success("激活成功", member);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取VIP信息")
    @GetMapping("/info")
    public Result<?> getInfo() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        VipMember member = vipService.getVipInfo(userId);
        if (member == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("isVip", false);
            return Result.success(data);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("isVip", member.getStatus() == 1);
        data.put("level", member.getLevel());
        data.put("levelName", getLevelName(member.getLevel()));
        data.put("expireTime", member.getExpireTime());
        data.put("heatCountToday", member.getHeatCountToday());
        data.put("downloadCountToday", member.getDownloadCountToday());
        data.put("heatLimit", getHeatLimit(member.getLevel()));
        data.put("downloadLimit", getDownloadLimit(member.getLevel()));
        return Result.success(data);
    }

    @Operation(summary = "获取VIP权益说明")
    @GetMapping("/privileges")
    public Result<?> getPrivileges() {
        return Result.success(vipService.getPrivileges());
    }

    @Operation(summary = "文章加热")
    @PostMapping("/heat/{articleId}")
    public Result<?> heatArticle(@PathVariable Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        try {
            vipService.heatArticle(userId, articleId);
            return Result.success("加热成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "下载文章MD")
    @GetMapping("/download/{articleId}")
    public ResponseEntity<?> downloadArticle(@PathVariable Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("请先登录");
        }
        
        try {
            vipService.checkDownload(userId, articleId);
            Article article = articleService.getById(articleId);
            if (article == null) {
                return ResponseEntity.badRequest().body("文章不存在");
            }
            
            String content = article.getContent();
            String filename = article.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".md";
            
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                            new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return securityUser.getUser().getId();
        }
        return null;
    }
    
    private String getLevelName(int level) {
        switch (level) {
            case 1: return "普通VIP";
            case 2: return "高级VIP";
            case 3: return "超级VIP";
            default: return "未知";
        }
    }
    
    private int getHeatLimit(int level) {
        switch (level) {
            case 1: return 3;
            case 2: return 10;
            case 3: return 30;
            default: return 0;
        }
    }
    
    private int getDownloadLimit(int level) {
        switch (level) {
            case 1: return 5;
            case 2: return 20;
            case 3: return -1;
            default: return 0;
        }
    }
}
