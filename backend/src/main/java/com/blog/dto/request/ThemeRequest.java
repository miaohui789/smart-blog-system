package com.blog.dto.request;

import lombok.Data;

@Data
public class ThemeRequest {
    private String themeMode;  // dark 或 light
    private String darkSkin;   // 暗色主题皮肤ID
    private String lightSkin;  // 亮色主题皮肤ID
}
