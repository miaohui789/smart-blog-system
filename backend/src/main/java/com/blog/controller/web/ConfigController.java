package com.blog.controller.web;

import com.blog.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "网站配置接口")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "获取网站公开配置")
    @GetMapping("/site")
    public Result<?> getSiteConfig() {
        List<Map<String, Object>> configs = jdbcTemplate.queryForList(
            "SELECT config_key, config_value FROM sys_config WHERE config_type = 'site'"
        );
        
        Map<String, Object> result = new HashMap<>();
        
        for (Map<String, Object> config : configs) {
            String key = (String) config.get("config_key");
            String value = (String) config.get("config_value");
            
            // 处理特殊key
            if ("site_icp".equals(key)) {
                result.put("icp", value);
            } else {
                // 转换key格式 site_name -> name
                String camelKey = toCamelCase(key.replace("site_", ""));
                result.put(camelKey, value);
            }
        }
        
        return Result.success(result);
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
