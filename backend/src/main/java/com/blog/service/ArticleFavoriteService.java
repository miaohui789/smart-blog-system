package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.ArticleFavorite;

public interface ArticleFavoriteService extends IService<ArticleFavorite> {
    boolean isFavorited(Long articleId, Long userId);
    boolean favorite(Long articleId, Long userId);
    boolean unfavorite(Long articleId, Long userId);
}
