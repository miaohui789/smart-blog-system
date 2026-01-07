package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.enums.ResultCode;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.ArticleRequest;
import com.blog.dto.response.ArticleVO;
import com.blog.dto.response.TagVO;
import com.blog.dto.response.UserVO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.User;
import com.blog.security.SecurityUser;
import com.blog.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    private final ArticleTagService articleTagService;
    private final TagService tagService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RedisService redisService;

    // Redis 缓存 Key 前缀
    private static final String CACHE_HOT_ARTICLES = "blog:hot:articles";
    private static final String CACHE_ARTICLE_VIEW = "blog:article:view:";
    private static final long CACHE_HOT_EXPIRE = 30; // 热门文章缓存30分钟

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
    public Result<ArticleVO> detail(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        
        // 使用 Redis 记录浏览量，避免频繁更新数据库
        try {
            String viewKey = CACHE_ARTICLE_VIEW + id;
            Long viewCount = redisService.increment(viewKey);
            
            // 每增加10次浏览量，同步到数据库
            if (viewCount != null && viewCount % 10 == 0) {
                article.setViewCount(article.getViewCount() + 10);
                articleService.updateById(article);
            }
            
            // 设置实时浏览量（数据库 + Redis增量）
            if (viewCount != null) {
                article.setViewCount(article.getViewCount() + viewCount.intValue() % 10);
            }
        } catch (Exception e) {
            // Redis 不可用时，直接更新数据库
            article.setViewCount(article.getViewCount() + 1);
            articleService.updateById(article);
        }
        
        // 转换为VO
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
            vo.setAuthor(authorVO);
        }
        
        // 获取分类名称
        if (article.getCategoryId() != null) {
            var category = categoryService.getById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        // 获取标签列表
        List<ArticleTag> articleTags = articleTagService.list(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id)
        );
        if (!articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
            List<com.blog.entity.Tag> tags = tagService.listByIds(tagIds);
            List<TagVO> tagVOs = tags.stream().map(tag -> {
                TagVO tagVO = new TagVO();
                tagVO.setId(tag.getId());
                tagVO.setName(tag.getName());
                tagVO.setColor(tag.getColor());
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOs);
        }
        
        // 获取当前用户的点赞和收藏状态
        Long userId = getCurrentUserId();
        if (userId != null) {
            vo.setIsLiked(articleLikeService.isLiked(id, userId));
            vo.setIsFavorited(articleFavoriteService.isFavorited(id, userId));
        } else {
            vo.setIsLiked(false);
            vo.setIsFavorited(false);
        }
        
        return Result.success(vo);
    }

    @Operation(summary = "热门文章")
    @GetMapping("/hot")
    @SuppressWarnings("unchecked")
    public Result<?> hot() {
        // 先从 Redis 缓存获取
        try {
            Object cached = redisService.get(CACHE_HOT_ARTICLES);
            if (cached != null) {
                return Result.success(cached);
            }
        } catch (Exception e) {
            // Redis 不可用，忽略缓存
        }
        
        // 缓存不存在或 Redis 不可用，从数据库查询
        List<Article> hotArticles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getViewCount)
                        .last("LIMIT 10")
        );
        
        // 尝试存入 Redis 缓存
        try {
            redisService.set(CACHE_HOT_ARTICLES, hotArticles, CACHE_HOT_EXPIRE, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis 不可用，忽略
        }
        
        return Result.success(hotArticles);
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

    @Operation(summary = "用户发布文章")
    @PostMapping
    public Result<?> create(@Valid @RequestBody ArticleRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        // 创建文章
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(request.getTitle());
        article.setCategoryId(request.getCategoryId());
        article.setCover(request.getCover());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setFavoriteCount(0);
        article.setIsTop(0);
        article.setIsRecommend(0);
        
        // 设置状态：0草稿 1发布
        Integer status = request.getStatus() != null ? request.getStatus() : 0;
        article.setStatus(status);
        
        // 如果是发布状态，设置发布时间
        if (status == 1) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        articleService.save(article);
        
        // 保存文章标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ArticleTag> articleTags = request.getTagIds().stream()
                    .map(tagId -> {
                        ArticleTag at = new ArticleTag();
                        at.setArticleId(article.getId());
                        at.setTagId(tagId);
                        return at;
                    })
                    .collect(Collectors.toList());
            articleTagService.saveBatch(articleTags);
        }
        
        return Result.success(article.getId());
    }

    @Operation(summary = "用户更新文章")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        // 检查文章是否存在且属于当前用户
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        if (!article.getUserId().equals(userId)) {
            return Result.error(ResultCode.FORBIDDEN, "无权修改此文章");
        }
        
        // 更新文章
        article.setTitle(request.getTitle());
        article.setCategoryId(request.getCategoryId());
        article.setCover(request.getCover());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        
        // 更新状态
        Integer oldStatus = article.getStatus();
        Integer newStatus = request.getStatus() != null ? request.getStatus() : oldStatus;
        article.setStatus(newStatus);
        
        // 如果从草稿变为发布，设置发布时间
        if (oldStatus == 0 && newStatus == 1) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        articleService.updateById(article);
        
        // 更新标签关联：先删除旧的，再添加新的
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ArticleTag> articleTags = request.getTagIds().stream()
                    .map(tagId -> {
                        ArticleTag at = new ArticleTag();
                        at.setArticleId(id);
                        at.setTagId(tagId);
                        return at;
                    })
                    .collect(Collectors.toList());
            articleTagService.saveBatch(articleTags);
        }
        
        return Result.success("更新成功");
    }

    @Operation(summary = "用户删除文章")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        // 检查文章是否存在且属于当前用户
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        if (!article.getUserId().equals(userId)) {
            return Result.error(ResultCode.FORBIDDEN, "无权删除此文章");
        }
        
        // 删除文章（逻辑删除）
        articleService.removeById(id);
        
        // 删除标签关联
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        
        return Result.success("删除成功");
    }

    @Operation(summary = "获取用户的文章列表")
    @GetMapping("/my")
    public Result<?> myArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(status != null, Article::getStatus, status)
                .orderByDesc(Article::getCreateTime);
        
        Page<Article> pageResult = articleService.page(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
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
