package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.enums.ResultCode;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.PasswordRequest;
import com.blog.dto.request.ProfileRequest;
import com.blog.entity.Article;
import com.blog.entity.ArticleFavorite;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.security.SecurityUser;
import com.blog.service.ArticleFavoriteService;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.FileService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "用户中心接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ArticleService articleService;
    private final ArticleFavoriteService articleFavoriteService;
    private final CommentService commentService;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public Result<?> getProfile() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        // 不返回密码
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody ProfileRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getIntro() != null) {
            user.setIntro(request.getIntro());
        }
        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite());
        }

        userService.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<?> updatePassword(@Validated @RequestBody PasswordRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateById(user);

        return Result.success("密码修改成功");
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        try {
            String url = fileService.uploadFile(file);
            
            User user = userService.getById(userId);
            user.setAvatar(url);
            userService.updateById(user);

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取我的收藏")
    @GetMapping("/favorites")
    public Result<?> getFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        // 获取用户收藏的文章ID列表
        List<Long> articleIds = articleFavoriteService.list(
                new LambdaQueryWrapper<ArticleFavorite>()
                        .eq(ArticleFavorite::getUserId, userId)
                        .orderByDesc(ArticleFavorite::getCreateTime)
        ).stream().map(ArticleFavorite::getArticleId).collect(Collectors.toList());

        if (articleIds.isEmpty()) {
            return Result.success(PageResult.of(List.of(), 0L, page, pageSize));
        }

        Page<Article> pageResult = articleService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Article>()
                        .in(Article::getId, articleIds)
                        .eq(Article::getStatus, 1)
        );

        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "获取我的评论")
    @GetMapping("/comments")
    public Result<?> getMyComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        Page<Comment> pageResult = commentService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, userId)
                        .eq(Comment::getStatus, 1)
                        .orderByDesc(Comment::getCreateTime)
        );

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
