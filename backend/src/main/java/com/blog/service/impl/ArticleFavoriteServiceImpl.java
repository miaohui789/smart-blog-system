package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Article;
import com.blog.entity.ArticleFavorite;
import com.blog.mapper.ArticleFavoriteMapper;
import com.blog.service.ArticleFavoriteService;
import com.blog.service.ArticleService;
import com.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleFavoriteServiceImpl extends ServiceImpl<ArticleFavoriteMapper, ArticleFavorite> implements ArticleFavoriteService {

    private final ArticleService articleService;
    private final RedisService redisService;

    @Override
    public boolean isFavorited(Long articleId, Long userId) {
        return count(new LambdaQueryWrapper<ArticleFavorite>()
                .eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getUserId, userId)) > 0;
    }

    @Override
    @Transactional
    public boolean favorite(Long articleId, Long userId) {
        if (isFavorited(articleId, userId)) {
            return false;
        }
        ArticleFavorite favorite = new ArticleFavorite();
        favorite.setArticleId(articleId);
        favorite.setUserId(userId);
        save(favorite);
        
        // 更新文章收藏数
        Article article = articleService.getById(articleId);
        if (article != null) {
            Integer currentCount = article.getFavoriteCount();
            article.setFavoriteCount(currentCount == null ? 1 : currentCount + 1);
            articleService.updateById(article);
            
            // 清除文章详情缓存
            redisService.clearArticleCache(articleId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean unfavorite(Long articleId, Long userId) {
        boolean removed = remove(new LambdaQueryWrapper<ArticleFavorite>()
                .eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getUserId, userId));
        
        if (removed) {
            Article article = articleService.getById(articleId);
            if (article != null) {
                Integer currentCount = article.getFavoriteCount();
                article.setFavoriteCount(currentCount != null && currentCount > 0 ? currentCount - 1 : 0);
                articleService.updateById(article);
                
                // 清除文章详情缓存
                redisService.clearArticleCache(articleId);
            }
        }
        return removed;
    }
}
