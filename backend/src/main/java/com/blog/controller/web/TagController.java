package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.service.ArticleTagService;
import com.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签接口")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final ArticleService articleService;
    private final ArticleTagService articleTagService;

    @Operation(summary = "标签列表")
    @GetMapping
    public Result<?> list() {
        return Result.success(tagService.list());
    }

    @Operation(summary = "标签下的文章")
    @GetMapping("/{id}/articles")
    public Result<?> articles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 获取该标签下的所有文章ID
        List<Long> articleIds = articleTagService.getArticleIdsByTagId(id);
        
        if (articleIds.isEmpty()) {
            return Result.success(PageResult.of(List.of(), 0L, page, pageSize));
        }
        
        Page<Article> pageResult = articleService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .in(Article::getId, articleIds)
                        .orderByDesc(Article::getPublishTime)
        );
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
    }
}
