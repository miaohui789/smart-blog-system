package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Article;
import com.blog.entity.ArticleLike;
import com.blog.mapper.ArticleLikeMapper;
import com.blog.service.ArticleLikeService;
import com.blog.service.ArticleService;
import com.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements ArticleLikeService {

    private final ArticleService articleService;
    private final RedisService redisService;

    @Override
    public boolean isLiked(Long articleId, Long userId) {
        return count(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId)) > 0;
    }

    @Override
    @Transactional
    public boolean like(Long articleId, Long userId) {
        if (isLiked(articleId, userId)) {
            return false;
        }
        ArticleLike like = new ArticleLike();
        like.setArticleId(articleId);
        like.setUserId(userId);
        save(like);
        
        // 更新文章点赞数
        Article article = articleService.getById(articleId);
        if (article != null) {
            Integer currentCount = article.getLikeCount();
            article.setLikeCount(currentCount == null ? 1 : currentCount + 1);
            articleService.updateById(article);
            
            // 清除文章详情缓存
            redisService.clearArticleCache(articleId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean unlike(Long articleId, Long userId) {
        boolean removed = remove(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId));
        
        if (removed) {
            Article article = articleService.getById(articleId);
            if (article != null) {
                Integer currentCount = article.getLikeCount();
                article.setLikeCount(currentCount != null && currentCount > 0 ? currentCount - 1 : 0);
                articleService.updateById(article);
                
                // 清除文章详情缓存
                redisService.clearArticleCache(articleId);
            }
        }
        return removed;
    }
}
