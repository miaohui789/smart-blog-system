package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.response.ArticleVO;
import com.blog.dto.response.UserProfileVO;
import com.blog.dto.response.UserVO;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.entity.UserFollow;
import com.blog.security.SecurityUser;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.NotificationService;
import com.blog.service.UserFollowService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "用户详情接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final UserFollowService userFollowService;
    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final NotificationService notificationService;

    @Operation(summary = "获取用户详情")
    @GetMapping("/{userId}/profile")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setIntro(user.getIntro());
        vo.setWebsite(user.getWebsite());
        vo.setUserLevel(user.getUserLevel());
        vo.setVipLevel(user.getVipLevel());
        vo.setVipExpireTime(user.getVipExpireTime());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        
        // 统计数据
        vo.setFollowCount(userFollowService.getFollowCount(userId));
        vo.setFansCount(userFollowService.getFansCount(userId));
        vo.setArticleCount(articleService.count(new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, 1)));
        
        // 当前用户是否已关注
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(userId)) {
            vo.setIsFollowed(userFollowService.isFollowed(currentUserId, userId));
        } else {
            vo.setIsFollowed(false);
        }
        
        return Result.success(vo);
    }

    @Operation(summary = "获取用户发布的文章")
    @GetMapping("/{userId}/articles")
    public Result<?> getUserArticles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, 1);
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getSummary, keyword));
        }
        
        // 排序
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        switch (sortBy) {
            case "view":
                wrapper.orderBy(true, isAsc, Article::getViewCount);
                break;
            case "like":
                wrapper.orderBy(true, isAsc, Article::getLikeCount);
                break;
            case "comment":
                wrapper.orderBy(true, isAsc, Article::getCommentCount);
                break;
            default:
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
        
        return Result.success(PageResult.of(voList, pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "关注用户")
    @PostMapping("/{userId}/follow")
    public Result<?> follow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        if (currentUserId.equals(userId)) {
            return Result.error("不能关注自己");
        }
        
        // 检查目标用户是否被冻结或已注销
        User targetUser = userService.getById(userId);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        if (targetUser.getStatus() != null && targetUser.getStatus() == 0) {
            return Result.error("该用户已被冻结，无法关注");
        }
        if (targetUser.getStatus() != null && targetUser.getStatus() == 2) {
            return Result.error("该用户已注销，无法关注");
        }
        
        boolean success = userFollowService.follow(currentUserId, userId);
        if (success) {
            // 发送关注通知
            notificationService.notifyFollowed(userId, currentUserId);
        }
        return success ? Result.success("关注成功") : Result.error("已经关注过了");
    }

    @Operation(summary = "取消关注")
    @DeleteMapping("/{userId}/follow")
    public Result<?> unfollow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        
        boolean success = userFollowService.unfollow(currentUserId, userId);
        return success ? Result.success("取消关注成功") : Result.error("未关注该用户");
    }

    @Operation(summary = "获取我的关注列表")
    @GetMapping("/following")
    public Result<?> getFollowingList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return getFollowingListByUserId(currentUserId, page, pageSize);
    }

    @Operation(summary = "获取指定用户的关注列表")
    @GetMapping("/{userId}/following")
    public Result<?> getUserFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return getFollowingListByUserId(userId, page, pageSize);
    }

    @Operation(summary = "获取我的粉丝列表")
    @GetMapping("/followers")
    public Result<?> getFollowersList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return getFollowersListByUserId(currentUserId, page, pageSize);
    }

    @Operation(summary = "获取指定用户的粉丝列表")
    @GetMapping("/{userId}/followers")
    public Result<?> getUserFollowersList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return getFollowersListByUserId(userId, page, pageSize);
    }

    // 获取指定用户的关注列表（内部方法）
    private Result<?> getFollowingListByUserId(Long userId, Integer page, Integer pageSize) {
        Long currentUserId = getCurrentUserId();
        
        // 获取关注的用户ID列表
        Page<UserFollow> followPage = userFollowService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getUserId, userId)
                        .orderByDesc(UserFollow::getCreateTime)
        );
        
        List<Long> userIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowUserId)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return Result.success(PageResult.of(List.of(), 0L, page, pageSize));
        }
        
        // 获取用户信息
        List<User> users = userService.listByIds(userIds);
        List<UserProfileVO> voList = users.stream().map(user -> {
            UserProfileVO vo = new UserProfileVO();
            vo.setId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setIntro(user.getIntro());
            vo.setUserLevel(user.getUserLevel());
            vo.setVipLevel(user.getVipLevel());
            // 如果当前用户已登录，检查是否关注了这个用户
            if (currentUserId != null) {
                vo.setIsFollowed(userFollowService.isFollowed(currentUserId, user.getId()));
            } else {
                vo.setIsFollowed(false);
            }
            vo.setArticleCount(articleService.count(new LambdaQueryWrapper<Article>()
                    .eq(Article::getUserId, user.getId())
                    .eq(Article::getStatus, 1)));
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(PageResult.of(voList, followPage.getTotal(), page, pageSize));
    }

    // 获取指定用户的粉丝列表（内部方法）
    private Result<?> getFollowersListByUserId(Long userId, Integer page, Integer pageSize) {
        Long currentUserId = getCurrentUserId();
        
        // 获取粉丝的用户ID列表
        Page<UserFollow> followPage = userFollowService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowUserId, userId)
                        .orderByDesc(UserFollow::getCreateTime)
        );
        
        List<Long> userIds = followPage.getRecords().stream()
                .map(UserFollow::getUserId)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return Result.success(PageResult.of(List.of(), 0L, page, pageSize));
        }
        
        // 获取用户信息
        List<User> users = userService.listByIds(userIds);
        List<UserProfileVO> voList = users.stream().map(user -> {
            UserProfileVO vo = new UserProfileVO();
            vo.setId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setIntro(user.getIntro());
            vo.setUserLevel(user.getUserLevel());
            vo.setVipLevel(user.getVipLevel());
            // 如果当前用户已登录，检查是否关注了这个用户
            if (currentUserId != null) {
                vo.setIsFollowed(userFollowService.isFollowed(currentUserId, user.getId()));
            } else {
                vo.setIsFollowed(false);
            }
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(PageResult.of(voList, followPage.getTotal(), page, pageSize));
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
