package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.enums.ResultCode;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.CommentRequest;
import com.blog.dto.response.CommentVO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.security.SecurityUser;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "评论接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ArticleService articleService;
    private final UserService userService;

    @Operation(summary = "获取文章评论列表")
    @GetMapping("/articles/{articleId}/comments")
    public Result<?> list(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 获取顶级评论
        Page<Comment> pageResult = commentService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .isNull(Comment::getParentId)
                        .orderByDesc(Comment::getCreateTime)
        );
        
        List<Comment> comments = pageResult.getRecords();
        if (comments.isEmpty()) {
            return Result.success(PageResult.of(new ArrayList<>(), 0L, page, pageSize));
        }
        
        // 获取所有评论的子评论
        List<Long> parentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> replies = commentService.list(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .in(Comment::getParentId, parentIds)
                        .orderByAsc(Comment::getCreateTime)
        );
        
        // 收集所有用户ID
        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        userIds.addAll(replies.stream().map(Comment::getUserId).collect(Collectors.toSet()));
        userIds.addAll(replies.stream()
                .filter(r -> r.getReplyUserId() != null)
                .map(Comment::getReplyUserId)
                .collect(Collectors.toSet()));
        
        // 批量查询用户信息
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // 按父评论ID分组子评论
        Map<Long, List<Comment>> repliesMap = replies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        
        // 转换为VO
        List<CommentVO> voList = comments.stream().map(comment -> {
            CommentVO vo = convertToVO(comment, userMap);
            List<Comment> childReplies = repliesMap.get(comment.getId());
            if (childReplies != null) {
                vo.setReplies(childReplies.stream()
                        .map(r -> convertToVO(r, userMap))
                        .collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(PageResult.of(voList, pageResult.getTotal(), page, pageSize));
    }
    
    private CommentVO convertToVO(Comment comment, Map<Long, User> userMap) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            CommentVO.UserInfo userInfo = new CommentVO.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            vo.setUser(userInfo);
        }
        
        if (comment.getReplyUserId() != null) {
            User replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                CommentVO.UserInfo replyUserInfo = new CommentVO.UserInfo();
                replyUserInfo.setId(replyUser.getId());
                replyUserInfo.setNickname(replyUser.getNickname());
                replyUserInfo.setAvatar(replyUser.getAvatar());
                vo.setReplyUser(replyUserInfo);
            }
        }
        
        return vo;
    }

    @Operation(summary = "发表评论")
    @PostMapping("/comments")
    public Result<?> create(@Validated @RequestBody CommentRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setReplyUserId(request.getReplyUserId());
        comment.setContent(request.getContent());
        comment.setStatus(1);
        comment.setLikeCount(0);
        commentService.save(comment);

        // 更新文章评论数
        Article article = articleService.getById(request.getArticleId());
        if (article != null) {
            article.setCommentCount(article.getCommentCount() + 1);
            articleService.updateById(article);
        }

        return Result.success("评论成功");
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }

        if (!comment.getUserId().equals(userId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }

        commentService.removeById(id);

        Article article = articleService.getById(comment.getArticleId());
        if (article != null && article.getCommentCount() > 0) {
            article.setCommentCount(article.getCommentCount() - 1);
            articleService.updateById(article);
        }

        return Result.success("删除成功");
    }

    @Operation(summary = "点赞评论")
    @PostMapping("/comments/{id}/like")
    public Result<?> like(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentService.updateById(comment);

        return Result.success("点赞成功");
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
