package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.enums.ResultCode;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.entity.Article;
import com.blog.security.SecurityUser;
import com.blog.service.ArticleFavoriteService;
import com.blog.service.ArticleLikeService;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "文章接口")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleLikeService articleLikeService;
    private final ArticleFavoriteService articleFavoriteService;

    @Operation(summary = "文章列表")
    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishTime);
        
        Page<Article> pageResult = articleService.page(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            articleService.updateById(article);
        }
        return Result.success(article);
    }

    @Operation(summary = "热门文章")
    @GetMapping("/hot")
    public Result<?> hot() {
        return Result.success(articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getViewCount)
                        .last("LIMIT 10")
        ));
    }

    @Operation(summary = "推荐文章")
    @GetMapping("/recommend")
    public Result<?> recommend() {
        return Result.success(articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .eq(Article::getIsRecommend, 1)
                        .orderByDesc(Article::getPublishTime)
                        .last("LIMIT 5")
        ));
    }

    @Operation(summary = "搜索文章")
    @GetMapping("/search")
    public Result<?> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Article> pageResult = articleService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .and(w -> w.like(Article::getTitle, keyword)
                                .or()
                                .like(Article::getSummary, keyword))
                        .orderByDesc(Article::getPublishTime)
        );
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "文章归档")
    @GetMapping("/archive")
    public Result<?> archive() {
        List<Article> articles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getPublishTime)
                        .select(Article::getId, Article::getTitle, Article::getPublishTime)
        );
        
        // 按年月分组
        Map<String, List<Article>> archiveMap = articles.stream()
                .collect(Collectors.groupingBy(
                        article -> {
                            LocalDateTime time = article.getPublishTime();
                            return time.getYear() + "-" + String.format("%02d", time.getMonthValue());
                        },
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        return Result.success(archiveMap);
    }

    @Operation(summary = "点赞文章")
    @PostMapping("/{id}/like")
    public Result<?> like(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        boolean success = articleLikeService.like(id, userId);
        return success ? Result.success("点赞成功") : Result.error("已经点赞过了");
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<?> unlike(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        boolean success = articleLikeService.unlike(id, userId);
        return success ? Result.success("取消点赞成功") : Result.error("未点赞");
    }

    @Operation(summary = "收藏文章")
    @PostMapping("/{id}/favorite")
    public Result<?> favorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        boolean success = articleFavoriteService.favorite(id, userId);
        return success ? Result.success("收藏成功") : Result.error("已经收藏过了");
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}/favorite")
    public Result<?> unfavorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        boolean success = articleFavoriteService.unfavorite(id, userId);
        return success ? Result.success("取消收藏成功") : Result.error("未收藏");
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return securityUser.getUser().getId();
        }
        return null;
    }
}
