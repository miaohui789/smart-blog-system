package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.dto.request.AdminAiScoreTestRequest;
import com.blog.entity.AiConfig;
import com.blog.entity.AiLogo;
import com.blog.service.AiChatService;
import com.blog.service.AiLogoService;
import com.blog.service.StudyAiScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理端-AI配置")
@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AdminAiConfigController {

    private final AiChatService aiChatService;
    private final AiLogoService aiLogoService;
    private final StudyAiScoreService studyAiScoreService;

    @Operation(summary = "获取所有AI配置列表")
    @GetMapping("/configs")
    public Result<?> getAllConfigs() {
        List<AiConfig> configs = aiChatService.getAllConfigs();
        Map<Long, String> logoIdMap = aiLogoService.getLogoIdMap();
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiConfig config : configs) {
            maskApiKey(config);
            Map<String, Object> item = new HashMap<>();
            item.put("id", config.getId());
            item.put("provider", config.getProvider());
            item.put("model", config.getModel());
            item.put("displayName", config.getDisplayName());
            item.put("apiKey", config.getApiKey());
            item.put("baseUrl", config.getBaseUrl());
            item.put("maxTokens", config.getMaxTokens());
            item.put("temperature", config.getTemperature());
            item.put("systemPrompt", config.getSystemPrompt());
            item.put("enabled", config.getEnabled());
            item.put("dailyLimit", config.getDailyLimit());
            item.put("sortOrder", config.getSortOrder());
            item.put("isDefault", config.getIsDefault());
            item.put("useForChat", config.getUseForChat());
            item.put("useForStudyScore", config.getUseForStudyScore());
            item.put("isDefaultStudyScore", config.getIsDefaultStudyScore());
            item.put("supportThinking", config.getSupportThinking());
            item.put("logoId", config.getLogoId());
            item.put("scoreSystemPrompt", config.getScoreSystemPrompt());
            // 通过 logoId 获取 Logo URL
            String logoUrl = config.getLogoId() != null ? logoIdMap.get(config.getLogoId()) : null;
            item.put("logoUrl", logoUrl);
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取单个AI配置")
    @GetMapping("/config/{id}")
    public Result<?> getConfigById(@PathVariable Long id) {
        AiConfig config = aiChatService.getConfigById(id);
        if (config != null) {
            maskApiKey(config);
        }
        return Result.success(config);
    }

    @Operation(summary = "获取AI配置（兼容旧接口）")
    @GetMapping("/config")
    public Result<?> getConfig() {
        AiConfig config = aiChatService.getConfig();
        if (config != null) {
            maskApiKey(config);
        }
        return Result.success(config);
    }

    @Operation(summary = "新增AI配置")
    @PostMapping("/config")
    public Result<?> addConfig(@RequestBody AiConfig config) {
        aiChatService.addConfig(config);
        return Result.success();
    }

    @Operation(summary = "更新AI配置")
    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody AiConfig config) {
        // 如果API Key是隐藏格式，保留原来的
        if (config.getApiKey() != null && config.getApiKey().contains("****")) {
            AiConfig oldConfig = aiChatService.getConfigById(config.getId());
            if (oldConfig != null) {
                config.setApiKey(oldConfig.getApiKey());
            }
        }
        aiChatService.updateConfig(config);
        return Result.success();
    }

    @Operation(summary = "删除AI配置")
    @DeleteMapping("/config/{id}")
    public Result<?> deleteConfig(@PathVariable Long id) {
        try {
            aiChatService.deleteConfig(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "设置默认模型")
    @PutMapping("/config/{id}/default")
    public Result<?> setDefault(@PathVariable Long id) {
        aiChatService.setDefaultConfig(id);
        return Result.success();
    }

    @Operation(summary = "设置学习模块AI评分默认模型")
    @PutMapping("/config/{id}/default-study-score")
    public Result<?> setStudyScoreDefault(@PathVariable Long id) {
        aiChatService.setDefaultStudyScoreConfig(id);
        return Result.success();
    }

    @Operation(summary = "测试AI连接")
    @PostMapping("/test")
    public Result<?> testConnection(@RequestBody(required = false) AiConfig testConfig) {
        AiConfig config = testConfig;
        if (config == null || config.getId() == null) {
            config = aiChatService.getConfig();
        } else {
            config = aiChatService.getConfigById(config.getId());
        }
        if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            return Result.error("请先配置API Key");
        }
        if (config.getBaseUrl() == null || config.getBaseUrl().isEmpty()) {
            return Result.error("请配置API地址");
        }
        return Result.success("配置有效");
    }

    @Operation(summary = "测试学习模块AI评分")
    @PostMapping("/test-study-score")
    public Result<?> testStudyScore(@Valid @RequestBody AdminAiScoreTestRequest request) {
        return Result.success(studyAiScoreService.testScore(request));
    }
    
    private void maskApiKey(AiConfig config) {
        if (config == null) return;
        String apiKey = config.getApiKey();
        if (apiKey != null && apiKey.length() > 8) {
            config.setApiKey(apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4));
        }
    }
}
