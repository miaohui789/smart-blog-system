package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.dto.response.DashboardVO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.Comment;
import com.blog.entity.Tag;
import com.blog.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@io.swagger.v3.oas.annotations.tags.Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ArticleService articleService;
    private final CommentService commentService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final JdbcTemplate jdbcTemplate;
    private final RedisService redisService;

    @Operation(summary = "统计数据")
    @GetMapping("/stats")
    public Result<DashboardVO> stats() {
        // 尝试从缓存获取
        DashboardVO cached = redisService.get(RedisService.CACHE_STATS, DashboardVO.class);
        if (cached != null) {
            return Result.success(cached);
        }
        
        DashboardVO vo = new DashboardVO();
        vo.setArticleCount(articleService.count());
        vo.setCommentCount(commentService.count());
        vo.setUserCount(userService.count());
        
        Long totalViews = articleService.list().stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();
        vo.setViewCount(totalViews);
        
        // 缓存5分钟
        redisService.setWithMinutes(RedisService.CACHE_STATS, vo, RedisService.EXPIRE_SHORT);
        
        return Result.success(vo);
    }

    @Operation(summary = "扩展统计数据")
    @GetMapping("/stats-extended")
    @SuppressWarnings("unchecked")
    public Result<?> statsExtended() {
        // 尝试从缓存获取
        String cacheKey = RedisService.CACHE_STATS + ":extended";
        Object cached = redisService.get(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }
        
        Map<String, Object> data = new HashMap<>();
        
        // 基础统计
        data.put("articleCount", articleService.count());
        data.put("commentCount", commentService.count());
        data.put("userCount", userService.count());
        data.put("categoryCount", categoryService.count());
        data.put("tagCount", tagService.count());
        
        // 已发布文章数
        LambdaQueryWrapper<Article> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Article::getStatus, 1);
        data.put("publishedCount", articleService.count(publishedWrapper));
        
        // 草稿数
        LambdaQueryWrapper<Article> draftWrapper = new LambdaQueryWrapper<>();
        draftWrapper.eq(Article::getStatus, 0);
        data.put("draftCount", articleService.count(draftWrapper));
        
        // 总浏览量
        Long totalViews = articleService.list().stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();
        data.put("viewCount", totalViews);
        
        // 总点赞数
        Long totalLikes = articleService.list().stream()
                .mapToLong(a -> a.getLikeCount() != null ? a.getLikeCount() : 0)
                .sum();
        data.put("likeCount", totalLikes);
        
        // 总收藏数
        Long totalFavorites = articleService.list().stream()
                .mapToLong(a -> a.getFavoriteCount() != null ? a.getFavoriteCount() : 0)
                .sum();
        data.put("favoriteCount", totalFavorites);
        
        // 待审核评论数
        LambdaQueryWrapper<Comment> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Comment::getStatus, 0);
        data.put("pendingCommentCount", commentService.count(pendingWrapper));
        
        // 缓存5分钟
        redisService.setWithMinutes(cacheKey, data, RedisService.EXPIRE_SHORT);
        
        return Result.success(data);
    }

    @Operation(summary = "访问趋势 - 最近7天")
    @GetMapping("/visit-trend")
    public Result<?> visitTrend() {
        // 获取最近7天的日期
        List<String> dates = new ArrayList<>();
        List<Integer> views = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            
            // 模拟基于文章发布时间的访问数据
            // 实际项目中应该有访问日志表
            int baseViews = 50 + (int)(Math.random() * 150);
            views.add(baseViews);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("xAxis", dates);
        data.put("series", views);
        return Result.success(data);
    }

    @Operation(summary = "文章发布趋势 - 最近30天")
    @GetMapping("/article-trend")
    public Result<?> articleTrend() {
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            
            // 查询当天发布的文章数
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Article::getPublishTime, startOfDay)
                   .lt(Article::getPublishTime, endOfDay)
                   .eq(Article::getStatus, 1);
            long count = articleService.count(wrapper);
            counts.add(count);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("xAxis", dates);
        data.put("series", counts);
        return Result.success(data);
    }

    @Operation(summary = "分类统计")
    @GetMapping("/category-stats")
    public Result<?> categoryStats() {
        List<Category> categories = categoryService.list();
        
        List<Map<String, Object>> result = categories.stream()
            .filter(c -> c.getArticleCount() != null && c.getArticleCount() > 0)
            .sorted((a, b) -> b.getArticleCount().compareTo(a.getArticleCount()))
            .limit(10)
            .map(category -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", category.getName());
                map.put("value", category.getArticleCount());
                return map;
            }).collect(Collectors.toList());
        
        // 如果没有数据，返回所有分类
        if (result.isEmpty()) {
            result = categories.stream().limit(10).map(category -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", category.getName());
                // 查询该分类下的文章数
                LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Article::getCategoryId, category.getId());
                map.put("value", articleService.count(wrapper));
                return map;
            }).collect(Collectors.toList());
        }
        
        return Result.success(result);
    }

    @Operation(summary = "标签统计")
    @GetMapping("/tag-stats")
    public Result<?> tagStats() {
        List<Tag> tags = tagService.list();
        
        List<Map<String, Object>> result = tags.stream()
            .sorted((a, b) -> {
                int countA = a.getArticleCount() != null ? a.getArticleCount() : 0;
                int countB = b.getArticleCount() != null ? b.getArticleCount() : 0;
                return countB - countA;
            })
            .limit(15)
            .map(tag -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", tag.getName());
                map.put("value", tag.getArticleCount() != null ? tag.getArticleCount() : 0);
                map.put("color", tag.getColor());
                return map;
            }).collect(Collectors.toList());
        
        return Result.success(result);
    }

    @Operation(summary = "文章状态分布")
    @GetMapping("/article-status")
    public Result<?> articleStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 已发布
        LambdaQueryWrapper<Article> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Article::getStatus, 1);
        Map<String, Object> published = new HashMap<>();
        published.put("name", "已发布");
        published.put("value", articleService.count(publishedWrapper));
        result.add(published);
        
        // 草稿
        LambdaQueryWrapper<Article> draftWrapper = new LambdaQueryWrapper<>();
        draftWrapper.eq(Article::getStatus, 0);
        Map<String, Object> draft = new HashMap<>();
        draft.put("name", "草稿");
        draft.put("value", articleService.count(draftWrapper));
        result.add(draft);
        
        // 已下架
        LambdaQueryWrapper<Article> offlineWrapper = new LambdaQueryWrapper<>();
        offlineWrapper.eq(Article::getStatus, 2);
        Map<String, Object> offline = new HashMap<>();
        offline.put("name", "已下架");
        offline.put("value", articleService.count(offlineWrapper));
        result.add(offline);
        
        return Result.success(result);
    }

    @Operation(summary = "评论状态分布")
    @GetMapping("/comment-status")
    public Result<?> commentStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 已通过
        LambdaQueryWrapper<Comment> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Comment::getStatus, 1);
        Map<String, Object> approved = new HashMap<>();
        approved.put("name", "已通过");
        approved.put("value", commentService.count(approvedWrapper));
        result.add(approved);
        
        // 待审核
        LambdaQueryWrapper<Comment> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Comment::getStatus, 0);
        Map<String, Object> pending = new HashMap<>();
        pending.put("name", "待审核");
        pending.put("value", commentService.count(pendingWrapper));
        result.add(pending);
        
        // 已拒绝
        LambdaQueryWrapper<Comment> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Comment::getStatus, 2);
        Map<String, Object> rejected = new HashMap<>();
        rejected.put("name", "已拒绝");
        rejected.put("value", commentService.count(rejectedWrapper));
        result.add(rejected);
        
        return Result.success(result);
    }

    @Operation(summary = "热门文章")
    @GetMapping("/hot-articles")
    public Result<?> hotArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
               .orderByDesc(Article::getViewCount)
               .last("LIMIT 10");
        List<Article> articles = articleService.list(wrapper);
        
        List<Map<String, Object>> result = articles.stream().map(article -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", article.getId());
            map.put("title", article.getTitle());
            map.put("viewCount", article.getViewCount() != null ? article.getViewCount() : 0);
            map.put("likeCount", article.getLikeCount() != null ? article.getLikeCount() : 0);
            map.put("commentCount", article.getCommentCount() != null ? article.getCommentCount() : 0);
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }

    @Operation(summary = "最新评论")
    @GetMapping("/recent-comments")
    public Result<?> recentComments() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime)
               .last("LIMIT 5");
        List<Comment> comments = commentService.list(wrapper);
        
        List<Map<String, Object>> result = comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("createTime", comment.getCreateTime());
            map.put("articleId", comment.getArticleId());
            
            // 获取文章标题
            if (comment.getArticleId() != null) {
                Article article = articleService.getById(comment.getArticleId());
                if (article != null) {
                    map.put("articleTitle", article.getTitle());
                }
            }
            
            // 获取用户信息
            if (comment.getUserId() != null) {
                com.blog.entity.User user = userService.getById(comment.getUserId());
                if (user != null) {
                    map.put("nickname", user.getNickname());
                    map.put("avatar", user.getAvatar());
                }
            }
            if (!map.containsKey("nickname")) {
                map.put("nickname", "匿名用户");
                map.put("avatar", null);
            }
            
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }

    @Operation(summary = "最新文章")
    @GetMapping("/recent-articles")
    public Result<?> recentArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
               .orderByDesc(Article::getPublishTime)
               .last("LIMIT 5");
        List<Article> articles = articleService.list(wrapper);
        
        List<Map<String, Object>> result = articles.stream().map(article -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", article.getId());
            map.put("title", article.getTitle());
            map.put("cover", article.getCover());
            map.put("publishTime", article.getPublishTime());
            map.put("viewCount", article.getViewCount() != null ? article.getViewCount() : 0);
            
            // 获取分类名称
            if (article.getCategoryId() != null) {
                Category category = categoryService.getById(article.getCategoryId());
                if (category != null) {
                    map.put("categoryName", category.getName());
                }
            }
            
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }

    @Operation(summary = "用户活跃度排行")
    @GetMapping("/active-users")
    public Result<?> activeUsers() {
        // 根据评论数统计活跃用户
        String sql = "SELECT u.id, u.nickname, u.avatar, COUNT(c.id) as comment_count " +
                     "FROM sys_user u " +
                     "LEFT JOIN blog_comment c ON u.id = c.user_id " +
                     "WHERE u.is_deleted = 0 " +
                     "GROUP BY u.id, u.nickname, u.avatar " +
                     "ORDER BY comment_count DESC " +
                     "LIMIT 10";
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return Result.success(result);
    }

    @Operation(summary = "今日数据概览")
    @GetMapping("/today-stats")
    public Result<?> todayStats() {
        Map<String, Object> data = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // 今日新增文章
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.ge(Article::getCreateTime, startOfDay)
                      .lt(Article::getCreateTime, endOfDay);
        data.put("newArticles", articleService.count(articleWrapper));
        
        // 今日新增评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.ge(Comment::getCreateTime, startOfDay)
                      .lt(Comment::getCreateTime, endOfDay);
        data.put("newComments", commentService.count(commentWrapper));
        
        // 今日新增用户
        try {
            String userSql = "SELECT COUNT(*) FROM sys_user WHERE create_time >= ? AND create_time < ? AND is_deleted = 0";
            Integer newUsers = jdbcTemplate.queryForObject(userSql, Integer.class, startOfDay, endOfDay);
            data.put("newUsers", newUsers != null ? newUsers : 0);
        } catch (Exception e) {
            data.put("newUsers", 0);
        }
        
        return Result.success(data);
    }
}
