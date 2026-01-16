package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.entity.AiConfig;
import com.blog.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-AI配置")
@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AdminAiConfigController {

    private final AiChatService aiChatService;

    @Operation(summary = "获取AI配置")
    @GetMapping("/config")
    public Result<?> getConfig() {
        AiConfig config = aiChatService.getConfig();
        if (config != null) {
            // 隐藏API Key中间部分
            String apiKey = config.getApiKey();
            if (apiKey != null && apiKey.length() > 8) {
                config.setApiKey(apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4));
            }
        }
        return Result.success(config);
    }

    @Operation(summary = "更新AI配置")
    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody AiConfig config) {
        // 如果API Key是隐藏格式，保留原来的
        if (config.getApiKey() != null && config.getApiKey().contains("****")) {
            AiConfig oldConfig = aiChatService.getConfig();
            if (oldConfig != null) {
                config.setApiKey(oldConfig.getApiKey());
            }
        }
        aiChatService.updateConfig(config);
        return Result.success();
    }

    @Operation(summary = "测试AI连接")
    @PostMapping("/test")
    public Result<?> testConnection() {
        AiConfig config = aiChatService.getConfig();
        if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            return Result.error("请先配置API Key");
        }
        // 简单测试：检查配置是否完整
        if (config.getBaseUrl() == null || config.getBaseUrl().isEmpty()) {
            return Result.error("请配置API地址");
        }
        return Result.success("配置有效");
    }
}
