package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.ArticleRequest;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "文章管理")
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章列表")
    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Article::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> pageResult = articleService.page(new Page<>(page, pageSize), wrapper);
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
        Article article = new Article();
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
        return Result.success("更新成功");
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        articleService.removeById(id);
        return Result.success("删除成功");
    }
}
