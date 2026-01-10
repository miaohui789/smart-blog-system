package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/api/admin/configs")
@RequiredArgsConstructor
public class AdminConfigController {

    private final JdbcTemplate jdbcTemplate;
    
    // 默认配置值
    private static final Map<String, String> DEFAULT_SITE_CONFIG = Map.of(
        "site_name", "我的博客",
        "site_description", "一个专注于技术分享的博客网站",
        "site_logo", "",
        "site_icp", ""
    );
    
    private static final Map<String, String> DEFAULT_OTHER_CONFIG = Map.of(
        "comment_audit", "0",
        "register_enabled", "1"
    );

    @Operation(summary = "获取配置")
    @GetMapping
    public Result<?> getConfigs() {
        List<Map<String, Object>> configs = jdbcTemplate.queryForList(
            "SELECT config_key, config_value, config_type FROM sys_config"
        );
        
        Map<String, Object> site = new HashMap<>();
        Map<String, Object> other = new HashMap<>();
        
        for (Map<String, Object> config : configs) {
            String key = (String) config.get("config_key");
            String value = (String) config.get("config_value");
            String type = (String) config.get("config_type");
            
            if ("site".equals(type)) {
                // 转换key格式 site_name -> siteName
                if ("site_icp".equals(key)) {
                    site.put("icp", value);
                } else {
                    // site_name -> siteName, site_description -> siteDescription
                    String camelKey = toCamelCase(key);
                    site.put(camelKey, value);
                }
            } else if ("comment".equals(type) || "user".equals(type)) {
                if ("comment_audit".equals(key)) {
                    other.put("commentAudit", "1".equals(value));
                } else if ("register_enabled".equals(key)) {
                    other.put("registerEnabled", "1".equals(value));
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("site", site);
        result.put("other", other);
        
        return Result.success(result);
    }

    @Operation(summary = "更新配置")
    @PutMapping
    public Result<?> updateConfigs(@RequestBody Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> site = (Map<String, Object>) data.get("site");
        @SuppressWarnings("unchecked")
        Map<String, Object> other = (Map<String, Object>) data.get("other");
        
        if (site != null) {
            updateConfig("site_name", String.valueOf(site.get("siteName")), "site");
            updateConfig("site_description", String.valueOf(site.get("siteDescription")), "site");
            updateConfig("site_logo", String.valueOf(site.get("siteLogo")), "site");
            if (site.get("icp") != null) {
                updateConfig("site_icp", String.valueOf(site.get("icp")), "site");
            }
        }
        
        if (other != null) {
            if (other.get("commentAudit") != null) {
                updateConfig("comment_audit", Boolean.TRUE.equals(other.get("commentAudit")) ? "1" : "0", "comment");
            }
            if (other.get("registerEnabled") != null) {
                updateConfig("register_enabled", Boolean.TRUE.equals(other.get("registerEnabled")) ? "1" : "0", "user");
            }
        }
        
        return Result.success("保存成功");
    }
    
    @Operation(summary = "恢复默认配置")
    @PostMapping("/reset/{type}")
    public Result<?> resetConfigs(@PathVariable String type) {
        if ("site".equals(type)) {
            for (Map.Entry<String, String> entry : DEFAULT_SITE_CONFIG.entrySet()) {
                updateConfig(entry.getKey(), entry.getValue(), "site");
            }
        } else if ("other".equals(type)) {
            for (Map.Entry<String, String> entry : DEFAULT_OTHER_CONFIG.entrySet()) {
                String configType = entry.getKey().startsWith("comment") ? "comment" : "user";
                updateConfig(entry.getKey(), entry.getValue(), configType);
            }
        }
        return Result.success("已恢复默认配置");
    }

    private void updateConfig(String key, String value, String type) {
        if (value == null || "null".equals(value)) return;
        
        int updated = jdbcTemplate.update(
            "UPDATE sys_config SET config_value = ? WHERE config_key = ?",
            value, key
        );
        
        if (updated == 0) {
            jdbcTemplate.update(
                "INSERT INTO sys_config (config_key, config_value, config_type) VALUES (?, ?, ?)",
                key, value, type
            );
        }
    }

    private String toCamelCase(String str) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}
