package com.blog.service;

import com.blog.entity.AiLogo;
import java.util.List;
import java.util.Map;

public interface AiLogoService {

    /**
     * 获取所有Logo列表
     */
    List<AiLogo> getAllLogos();

    /**
     * 根据ID获取Logo
     */
    AiLogo getLogoById(Long id);

    /**
     * 根据服务商获取Logo
     */
    AiLogo getLogoByProvider(String provider);

    /**
     * 获取所有服务商Logo映射（provider -> logoUrl）
     */
    Map<String, String> getProviderLogoMap();

    /**
     * 获取所有Logo映射（id -> logoUrl）
     */
    Map<Long, String> getLogoIdMap();

    /**
     * 新增Logo
     */
    void addLogo(AiLogo logo);

    /**
     * 更新Logo
     */
    void updateLogo(AiLogo logo);

    /**
     * 删除Logo
     */
    void deleteLogo(Long id);
}
