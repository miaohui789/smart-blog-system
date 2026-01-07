package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.ArticleLike;

public interface ArticleLikeService extends IService<ArticleLike> {
    boolean isLiked(Long articleId, Long userId);
    boolean like(Long articleId, Long userId);
    boolean unlike(Long articleId, Long userId);
}
