package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.response.TagVO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.service.ArticleService;
import com.blog.service.ArticleTagService;
import com.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 获取所有标签
        List<com.blog.entity.Tag> tags = tagService.list();
        
        // 统计每个标签的文章数量
        List<ArticleTag> allArticleTags = articleTagService.list();
        Map<Long, Long> tagArticleCountMap = allArticleTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getTagId, Collectors.counting()));
        
        // 转换为 VO 并设置真实的文章数量
        List<TagVO> tagVOs = tags.stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setColor(tag.getColor());
            vo.setArticleCount(tagArticleCountMap.getOrDefault(tag.getId(), 0L).intValue());
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(tagVOs);
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
