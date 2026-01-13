package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.ArticleRequest;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.security.SecurityUser;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "文章管理")
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final RedisService redisService;

    @Operation(summary = "文章列表")
    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Article::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        wrapper.orderByDesc(Article::getIsTop).orderByDesc(Article::getCreateTime);

        Page<Article> pageResult = articleService.page(new Page<>(page, pageSize), wrapper);
        
        // 填充分类名称
        pageResult.getRecords().forEach(article -> {
            if (article.getCategoryId() != null) {
                Category category = categoryService.getById(article.getCategoryId());
                if (category != null) {
                    article.setCategoryName(category.getName());
                }
            }
        });
        
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @Operation(summary = "创建文章")
    @PostMapping
    public Result<?> create(@Validated @RequestBody ArticleRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(request.getTitle());
        article.setCategoryId(request.getCategoryId());
        article.setCover(request.getCover());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setFavoriteCount(0);
        article.setIsTop(0);
        article.setIsRecommend(0);
        if (article.getStatus() == 1) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleService.save(article);
        
        // 清除文章相关缓存
        redisService.clearArticleCache(article.getId());
        
        return Result.success("创建成功");
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Validated @RequestBody ArticleRequest request) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        article.setTitle(request.getTitle());
        article.setCategoryId(request.getCategoryId());
        article.setCover(request.getCover());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        if (request.getStatus() != null) {
            if (request.getStatus() == 1 && article.getPublishTime() == null) {
                article.setPublishTime(LocalDateTime.now());
            }
            article.setStatus(request.getStatus());
        }
        articleService.updateById(article);
        
        // 清除文章相关缓存
        redisService.clearArticleCache(id);
        
        return Result.success("更新成功");
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        articleService.removeById(id);
        
        // 清除文章相关缓存
        redisService.clearArticleCache(id);
        
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除文章")
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids != null && !ids.isEmpty()) {
            articleService.removeByIds(ids);
            // 清除所有文章相关缓存
            ids.forEach(redisService::clearArticleCache);
        }
        return Result.success("删除成功");
    }

    @Operation(summary = "更新文章状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        Integer status = body.get("status");
        article.setStatus(status);
        if (status == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleService.updateById(article);
        
        // 清除文章相关缓存
        redisService.clearArticleCache(id);
        
        return Result.success("更新成功");
    }

    @Operation(summary = "设置置顶")
    @PutMapping("/{id}/top")
    public Result<?> updateTop(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        article.setIsTop(body.get("isTop"));
        articleService.updateById(article);
        
        // 清除文章相关缓存
        redisService.clearArticleCache(id);
        
        return Result.success("更新成功");
    }

    @Operation(summary = "设置推荐")
    @PutMapping("/{id}/recommend")
    public Result<?> updateRecommend(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        article.setIsRecommend(body.get("isRecommend"));
        articleService.updateById(article);
        
        // 清除文章相关缓存
        redisService.clearArticleCache(id);
        
        return Result.success("更新成功");
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
