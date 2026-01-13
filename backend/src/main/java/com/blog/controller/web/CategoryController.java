package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ArticleService articleService;
    private final RedisService redisService;

    @Operation(summary = "分类列表")
    @GetMapping
    @SuppressWarnings("unchecked")
    public Result<?> list() {
        // 先从 Redis 缓存获取
        Object cached = redisService.get(RedisService.CACHE_CATEGORY_LIST);
        if (cached != null) {
            return Result.success(cached);
        }
        
        // 获取所有分类
        var categories = categoryService.list();
        // 动态计算每个分类的文章数量
        categories.forEach(category -> {
            long count = articleService.count(
                new LambdaQueryWrapper<Article>()
                    .eq(Article::getStatus, 1)
                    .eq(Article::getCategoryId, category.getId())
            );
            category.setArticleCount((int) count);
        });
        
        // 存入 Redis 缓存（1小时）
        redisService.setWithMinutes(RedisService.CACHE_CATEGORY_LIST, categories, RedisService.EXPIRE_LONG);
        
        return Result.success(categories);
    }

    @Operation(summary = "分类下的文章")
    @GetMapping("/{id}/articles")
    public Result<?> articles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 尝试从缓存获取
        String cacheKey = RedisService.CACHE_CATEGORY + id + ":articles:" + page + ":" + pageSize;
        Object cached = redisService.get(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }
        
        Page<Article> pageResult = articleService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .eq(Article::getCategoryId, id)
                        .orderByDesc(Article::getPublishTime)
        );
        
        PageResult<Article> result = PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize);
        
        // 存入 Redis 缓存（5分钟）
        redisService.setWithMinutes(cacheKey, result, RedisService.EXPIRE_SHORT);
        
        return Result.success(result);
    }
}
