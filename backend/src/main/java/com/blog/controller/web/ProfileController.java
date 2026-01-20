package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.enums.ResultCode;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.PasswordRequest;
import com.blog.dto.request.ProfileRequest;
import com.blog.dto.request.ThemeRequest;
import com.blog.dto.response.ArticleVO;
import com.blog.dto.response.CommentVO;
import com.blog.dto.response.UserVO;
import com.blog.entity.Article;
import com.blog.entity.ArticleFavorite;
import com.blog.entity.ArticleLike;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.security.SecurityUser;
import com.blog.service.ArticleFavoriteService;
import com.blog.service.ArticleLikeService;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.FileService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
    private final ArticleLikeService articleLikeService;
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

        // 查询文章列表（只查询已发布的文章）
        Page<Article> pageResult = articleService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Article>()
                        .in(Article::getId, articleIds)
                        .eq(Article::getStatus, 1)
        );

        // 转换为VO，包含作者信息
        List<ArticleVO> articleVOList = pageResult.getRecords().stream()
                .map(article -> {
                    ArticleVO vo = new ArticleVO();
                    BeanUtils.copyProperties(article, vo);
                    
                    // 获取作者信息
                    User author = userService.getById(article.getUserId());
                    if (author != null) {
                        UserVO authorVO = new UserVO();
                        authorVO.setId(author.getId());
                        authorVO.setNickname(author.getNickname());
                        authorVO.setAvatar(author.getAvatar());
                        authorVO.setVipLevel(author.getVipLevel());
                        vo.setAuthor(authorVO);
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());

        // 使用实际的收藏总数，而不是分页查询的总数
        return Result.success(PageResult.of(articleVOList, (long) articleIds.size(), page, pageSize));
    }

    @Operation(summary = "获取我的点赞")
    @GetMapping("/likes")
    public Result<?> getLikes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        // 获取用户点赞的文章ID列表
        List<Long> articleIds = articleLikeService.list(
                new LambdaQueryWrapper<ArticleLike>()
                        .eq(ArticleLike::getUserId, userId)
                        .orderByDesc(ArticleLike::getCreateTime)
        ).stream().map(ArticleLike::getArticleId).collect(Collectors.toList());

        if (articleIds.isEmpty()) {
            return Result.success(PageResult.of(List.of(), 0L, page, pageSize));
        }

        // 查询文章列表（只查询已发布的文章）
        Page<Article> pageResult = articleService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Article>()
                        .in(Article::getId, articleIds)
                        .eq(Article::getStatus, 1)
        );

        // 转换为VO，包含作者信息
        List<ArticleVO> articleVOList = pageResult.getRecords().stream()
                .map(article -> {
                    ArticleVO vo = new ArticleVO();
                    BeanUtils.copyProperties(article, vo);
                    
                    // 获取作者信息
                    User author = userService.getById(article.getUserId());
                    if (author != null) {
                        UserVO authorVO = new UserVO();
                        authorVO.setId(author.getId());
                        authorVO.setNickname(author.getNickname());
                        authorVO.setAvatar(author.getAvatar());
                        authorVO.setVipLevel(author.getVipLevel());
                        vo.setAuthor(authorVO);
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());

        // 使用实际的点赞总数，而不是分页查询的总数
        return Result.success(PageResult.of(articleVOList, (long) articleIds.size(), page, pageSize));
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

        // 转换为VO，包含文章信息
        List<CommentVO> commentVOList = pageResult.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = new CommentVO();
                    BeanUtils.copyProperties(comment, vo);
                    
                    // 获取文章信息
                    Article article = articleService.getById(comment.getArticleId());
                    if (article != null) {
                        CommentVO.ArticleInfo articleInfo = new CommentVO.ArticleInfo();
                        articleInfo.setId(article.getId());
                        articleInfo.setTitle(article.getTitle());
                        articleInfo.setCover(article.getCover());
                        articleInfo.setViewCount(article.getViewCount());
                        articleInfo.setLikeCount(article.getLikeCount());
                        vo.setArticle(articleInfo);
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());

        return Result.success(PageResult.of(commentVOList, pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "保存主题设置")
    @PutMapping("/theme")
    public Result<?> updateTheme(@RequestBody ThemeRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        // 更新主题设置
        if (request.getThemeMode() != null) {
            user.setThemeMode(request.getThemeMode());
        }
        if (request.getDarkSkin() != null) {
            user.setDarkSkin(request.getDarkSkin());
        }
        if (request.getLightSkin() != null) {
            user.setLightSkin(request.getLightSkin());
        }

        userService.updateById(user);
        return Result.success("主题设置已保存");
    }

    @Operation(summary = "获取主题设置")
    @GetMapping("/theme")
    public Result<?> getTheme() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        Map<String, String> theme = new HashMap<>();
        theme.put("themeMode", user.getThemeMode() != null ? user.getThemeMode() : "dark");
        theme.put("darkSkin", user.getDarkSkin() != null ? user.getDarkSkin() : "default");
        theme.put("lightSkin", user.getLightSkin() != null ? user.getLightSkin() : "default");
        
        return Result.success(theme);
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
