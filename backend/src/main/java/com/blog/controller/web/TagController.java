package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.response.ArticleVO;
import com.blog.dto.response.TagVO;
import com.blog.dto.response.UserVO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.ArticleTagService;
import com.blog.service.CategoryService;
import com.blog.service.RedisService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
    private final UserService userService;
    private final CategoryService categoryService;
    private final RedisService redisService;

    @Operation(summary = "标签列表")
    @GetMapping
    @SuppressWarnings("unchecked")
    public Result<?> list() {
        // 先从 Redis 缓存获取
        Object cached = redisService.get(RedisService.CACHE_TAG_LIST);
        if (cached != null) {
            return Result.success(cached);
        }
        
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
        
        // 存入 Redis 缓存（1小时）
        redisService.setWithMinutes(RedisService.CACHE_TAG_LIST, tagVOs, RedisService.EXPIRE_LONG);
        
        return Result.success(tagVOs);
    }

    @Operation(summary = "标签下的文章")
    @GetMapping("/{id}/articles")
    public Result<?> articles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 尝试从缓存获取
        String cacheKey = RedisService.CACHE_TAG + id + ":articles:" + page + ":" + pageSize;
        Object cached = redisService.get(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }
        
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
        
        // 转换为 VO 并填充作者信息
        List<ArticleVO> voList = pageResult.getRecords().stream().map(article -> {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);
            
            // 获取作者信息
            User author = userService.getById(article.getUserId());
            if (author != null) {
                UserVO authorVO = new UserVO();
                authorVO.setId(author.getId());
                authorVO.setUsername(author.getUsername());
                authorVO.setNickname(author.getNickname());
                authorVO.setAvatar(author.getAvatar());
                authorVO.setUserLevel(author.getUserLevel());
                authorVO.setVipLevel(author.getVipLevel());
                vo.setAuthor(authorVO);
            }
            
            // 获取分类名称
            if (article.getCategoryId() != null) {
                var category = categoryService.getById(article.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        PageResult<ArticleVO> result = PageResult.of(voList, pageResult.getTotal(), page, pageSize);
        
        // 存入 Redis 缓存（5分钟）
        redisService.setWithMinutes(cacheKey, result, RedisService.EXPIRE_SHORT);
        
        return Result.success(result);
    }
}
