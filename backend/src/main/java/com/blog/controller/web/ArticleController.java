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
import com.blog.utils.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final NotificationService notificationService;

    @Operation(summary = "文章列表")
    @GetMapping
    public Result<PageResult<ArticleVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer excludeTop,
            @RequestParam(required = false) Integer onlyTop) {
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .eq(categoryId != null, Article::getCategoryId, categoryId);
        
        // 处理置顶筛选
        if (onlyTop != null && onlyTop == 1) {
            wrapper.eq(Article::getIsTop, 1);
        } else if (excludeTop != null && excludeTop == 1) {
            wrapper.eq(Article::getIsTop, 0);
        }
        
        // 根据排序参数设置排序
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        switch (sortBy) {
            case "latest":
                wrapper.orderByDesc(Article::getIsTop)
                       .orderByDesc(Article::getPublishTime);
                break;
            case "heat":
                if (isAsc) {
                    wrapper.orderByAsc(Article::getHeatCount);
                } else {
                    wrapper.orderByDesc(Article::getHeatCount);
                }
                break;
            case "view":
                if (isAsc) {
                    wrapper.orderByAsc(Article::getViewCount);
                } else {
                    wrapper.orderByDesc(Article::getViewCount);
                }
                break;
            case "comment":
                if (isAsc) {
                    wrapper.orderByAsc(Article::getCommentCount);
                } else {
                    wrapper.orderByDesc(Article::getCommentCount);
                }
                break;
            case "like":
                if (isAsc) {
                    wrapper.orderByAsc(Article::getLikeCount);
                } else {
                    wrapper.orderByDesc(Article::getLikeCount);
                }
                break;
            case "favorite":
                if (isAsc) {
                    wrapper.orderByAsc(Article::getFavoriteCount);
                } else {
                    wrapper.orderByDesc(Article::getFavoriteCount);
                }
                break;
            default:
                wrapper.orderByDesc(Article::getIsTop)
                       .orderByDesc(Article::getPublishTime);
                break;
        }
        
        if (!"latest".equals(sortBy) && !"default".equals(sortBy)) {
            wrapper.orderByDesc(Article::getPublishTime);
        }
        
        Page<Article> pageResult = articleService.page(new Page<>(page, pageSize), wrapper);
        
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
        
        return Result.success(result);
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id, HttpServletRequest request) {
        // 先从 Redis 缓存获取
        String cacheKey = RedisService.CACHE_ARTICLE + id;
        ArticleVO cachedVO = redisService.get(cacheKey, ArticleVO.class);
        
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        
        // 获取客户端IP，用于防刷
        String clientIp = IpUtils.getIpAddress(request);
        String viewLimitKey = RedisService.CACHE_VIEW_COUNT + "limit:" + id + ":" + clientIp;
        
        // 检查该IP是否在5分钟内已经访问过该文章
        boolean hasViewed = redisService.hasKey(viewLimitKey);
        
        if (!hasViewed) {
            // 设置访问标记，5分钟内同一IP访问同一文章不重复计数
            redisService.setWithMinutes(viewLimitKey, "1", 5);
            
            // 直接更新数据库浏览量
            article.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
            articleService.updateById(article);
            
            // 清除文章列表缓存，确保列表页显示最新数据
            redisService.deleteByPattern(RedisService.CACHE_ARTICLE_LIST + "*");
        }
        
        // 获取当前用户ID
        Long userId = getCurrentUserId();
        
        // 直接使用数据库中的阅览量
        int currentViewCount = article.getViewCount() != null ? article.getViewCount() : 0;
        
        // 如果有缓存，补充用户相关状态后返回
        if (cachedVO != null) {
            cachedVO.setViewCount(currentViewCount);
            // 补充当前用户的点赞和收藏状态
            if (userId != null) {
                cachedVO.setIsLiked(articleLikeService.isLiked(id, userId));
                cachedVO.setIsFavorited(articleFavoriteService.isFavorited(id, userId));
            } else {
                cachedVO.setIsLiked(false);
                cachedVO.setIsFavorited(false);
            }
            // 更新作者状态（可能被冻结）
            if (cachedVO.getAuthor() != null) {
                User author = userService.getById(article.getUserId());
                if (author != null) {
                    cachedVO.getAuthor().setStatus(author.getStatus());
                }
            }
            return Result.success(cachedVO);
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
            authorVO.setStatus(author.getStatus());
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
        if (userId != null) {
            vo.setIsLiked(articleLikeService.isLiked(id, userId));
            vo.setIsFavorited(articleFavoriteService.isFavorited(id, userId));
        } else {
            vo.setIsLiked(false);
            vo.setIsFavorited(false);
        }
        
        // 设置实时阅览量
        vo.setViewCount(currentViewCount);
        
        // 存入 Redis 缓存（30分钟）- 不缓存用户相关状态
        ArticleVO cacheVO = new ArticleVO();
        BeanUtils.copyProperties(vo, cacheVO);
        cacheVO.setIsLiked(null);
        cacheVO.setIsFavorited(null);
        redisService.setWithMinutes(cacheKey, cacheVO, RedisService.EXPIRE_MEDIUM);
        
        return Result.success(vo);
    }

    @Operation(summary = "热门文章")
    @GetMapping("/hot")
    public Result<?> hot() {
        // 先从 Redis 缓存获取
        Object cached = redisService.get(RedisService.CACHE_HOT_ARTICLES);
        if (cached != null) {
            return Result.success(cached);
        }
        
        // 缓存不存在，从数据库查询
        List<Article> hotArticles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getViewCount)
                        .last("LIMIT 10")
        );
        
        // 存入 Redis 缓存（30分钟）
        redisService.setWithMinutes(RedisService.CACHE_HOT_ARTICLES, hotArticles, RedisService.EXPIRE_MEDIUM);
        
        return Result.success(hotArticles);
    }

    @Operation(summary = "推荐文章")
    @GetMapping("/recommend")
    public Result<?> recommend() {
        // 先从 Redis 缓存获取
        Object cached = redisService.get(RedisService.CACHE_RECOMMEND_ARTICLES);
        if (cached != null) {
            return Result.success(cached);
        }
        
        // 缓存不存在，从数据库查询
        List<Article> recommendArticles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .eq(Article::getIsRecommend, 1)
                        .orderByDesc(Article::getPublishTime)
                        .last("LIMIT 5")
        );
        
        // 存入 Redis 缓存（30分钟）
        redisService.setWithMinutes(RedisService.CACHE_RECOMMEND_ARTICLES, recommendArticles, RedisService.EXPIRE_MEDIUM);
        
        return Result.success(recommendArticles);
    }

    @Operation(summary = "搜索文章")
    @GetMapping("/search")
    public Result<?> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 1. 先查找匹配关键词的标签
        List<com.blog.entity.Tag> matchedTags = tagService.list(
                new LambdaQueryWrapper<com.blog.entity.Tag>()
                        .like(com.blog.entity.Tag::getName, keyword)
        );
        
        // 2. 获取这些标签关联的文章ID
        List<Long> tagArticleIds = List.of();
        if (!matchedTags.isEmpty()) {
            List<Long> tagIds = matchedTags.stream().map(com.blog.entity.Tag::getId).collect(Collectors.toList());
            tagArticleIds = articleTagService.list(
                    new LambdaQueryWrapper<ArticleTag>()
                            .in(ArticleTag::getTagId, tagIds)
            ).stream().map(ArticleTag::getArticleId).distinct().collect(Collectors.toList());
        }
        
        // 3. 构建查询条件：标题、摘要、内容匹配 OR 标签匹配
        final List<Long> finalTagArticleIds = tagArticleIds;
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .and(w -> {
                    w.like(Article::getTitle, keyword)
                            .or()
                            .like(Article::getSummary, keyword)
                            .or()
                            .like(Article::getContent, keyword);
                    // 如果有标签匹配的文章，也加入结果
                    if (!finalTagArticleIds.isEmpty()) {
                        w.or().in(Article::getId, finalTagArticleIds);
                    }
                })
                .orderByDesc(Article::getPublishTime);
        
        Page<Article> pageResult = articleService.page(new Page<>(page, pageSize), wrapper);
        
        // 4. 为搜索结果添加作者和标签信息
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
            
            // 获取文章的标签
            List<ArticleTag> articleTags = articleTagService.list(
                    new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId())
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
            
            return vo;
        }).collect(Collectors.toList());
        
        // 5. 搜索用户（最多返回5个）
        List<User> users = userService.searchUsers(keyword, 5);
        List<UserVO> userVOList = users.stream().map(user -> {
            UserVO uvo = new UserVO();
            uvo.setId(user.getId());
            uvo.setUsername(user.getUsername());
            uvo.setNickname(user.getNickname());
            uvo.setAvatar(user.getAvatar());
            uvo.setIntro(user.getIntro());
            uvo.setVipLevel(user.getVipLevel());
            return uvo;
        }).collect(Collectors.toList());
        
        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("articles", PageResult.of(voList, pageResult.getTotal(), page, pageSize));
        result.put("users", userVOList);
        
        return Result.success(result);
    }

    @Operation(summary = "文章归档")
    @GetMapping("/archive")
    public Result<?> archive() {
        // 先从 Redis 缓存获取
        Object cached = redisService.get(RedisService.CACHE_ARCHIVE);
        if (cached != null) {
            return Result.success(cached);
        }
        
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
        
        // 存入 Redis 缓存（1小时）
        redisService.setWithMinutes(RedisService.CACHE_ARCHIVE, archiveMap, RedisService.EXPIRE_LONG);
        
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
        if (success) {
            // 发送点赞通知
            Article article = articleService.getById(id);
            if (article != null && !article.getUserId().equals(userId)) {
                notificationService.notifyArticleLiked(article.getUserId(), userId, id, article.getTitle());
            }
            // 返回更新后的点赞数
            Article updatedArticle = articleService.getById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("likeCount", updatedArticle.getLikeCount());
            return Result.success("点赞成功", data);
        }
        return Result.error("已经点赞过了");
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<?> unlike(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        boolean success = articleLikeService.unlike(id, userId);
        if (success) {
            // 返回更新后的点赞数
            Article updatedArticle = articleService.getById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("likeCount", updatedArticle.getLikeCount());
            return Result.success("取消点赞成功", data);
        }
        return Result.error("未点赞");
    }

    @Operation(summary = "收藏文章")
    @PostMapping("/{id}/favorite")
    public Result<?> favorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        boolean success = articleFavoriteService.favorite(id, userId);
        if (success) {
            // 发送收藏通知
            Article article = articleService.getById(id);
            if (article != null && !article.getUserId().equals(userId)) {
                notificationService.notifyArticleFavorited(article.getUserId(), userId, id, article.getTitle());
            }
            // 返回更新后的收藏数
            Article updatedArticle = articleService.getById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("favoriteCount", updatedArticle.getFavoriteCount());
            return Result.success("收藏成功", data);
        }
        return Result.error("已经收藏过了");
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}/favorite")
    public Result<?> unfavorite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        boolean success = articleFavoriteService.unfavorite(id, userId);
        if (success) {
            // 返回更新后的收藏数
            Article updatedArticle = articleService.getById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("favoriteCount", updatedArticle.getFavoriteCount());
            return Result.success("取消收藏成功", data);
        }
        return Result.error("未收藏");
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
        
        // 清除文章相关缓存
        redisService.clearArticleCache(article.getId());
        
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
        
        // 清除文章相关缓存
        redisService.clearArticleCache(id);
        
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
        
        // 清除文章相关缓存
        redisService.clearArticleCache(id);
        
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
