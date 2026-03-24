package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.AiLogo;
import com.blog.mapper.AiLogoMapper;
import com.blog.service.AiLogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiLogoServiceImpl implements AiLogoService {

    private final AiLogoMapper aiLogoMapper;

    @Override
    public List<AiLogo> getAllLogos() {
        return aiLogoMapper.selectList(
            new LambdaQueryWrapper<AiLogo>()
                .orderByAsc(AiLogo::getSortOrder)
                .orderByDesc(AiLogo::getCreateTime)
        );
    }

    @Override
    public AiLogo getLogoById(Long id) {
        return aiLogoMapper.selectById(id);
    }

    @Override
    public AiLogo getLogoByProvider(String provider) {
        return aiLogoMapper.selectOne(
            new LambdaQueryWrapper<AiLogo>()
                .eq(AiLogo::getProvider, provider)
                .last("LIMIT 1")
        );
    }

    @Override
    public Map<String, String> getProviderLogoMap() {
        List<AiLogo> logos = getAllLogos();
        Map<String, String> map = new HashMap<>();
        for (AiLogo logo : logos) {
            // 每个 provider 只取第一个（排序靠前的）
            if (!map.containsKey(logo.getProvider())) {
                map.put(logo.getProvider(), logo.getLogoUrl());
            }
        }
        return map;
    }

    @Override
    public Map<Long, String> getLogoIdMap() {
        List<AiLogo> logos = getAllLogos();
        Map<Long, String> map = new HashMap<>();
        for (AiLogo logo : logos) {
            map.put(logo.getId(), logo.getLogoUrl());
        }
        return map;
    }

    @Override
    public void addLogo(AiLogo logo) {
        aiLogoMapper.insert(logo);
    }

    @Override
    public void updateLogo(AiLogo logo) {
        aiLogoMapper.updateById(logo);
    }

    @Override
    public void deleteLogo(Long id) {
        aiLogoMapper.deleteById(id);
    }
}
