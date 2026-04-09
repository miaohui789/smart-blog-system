package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.constant.ExpConstants;
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
import com.blog.service.NotificationService;
import com.blog.service.RedisService;
import com.blog.service.SearchService;
import com.blog.service.UserService;
import com.blog.mq.UserExpAsyncService;
import com.blog.websocket.WebSocketServer;
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
    private final NotificationService notificationService;
    private final WebSocketServer webSocketServer;
    private final UserExpAsyncService userExpAsyncService;
    private final RedisService redisService;
    private final SearchService searchService;

    @Operation(summary = "获取文章评论列表")
    @GetMapping("/articles/{articleId}/comments")
    public Result<?> list(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 获取顶级评论 (parentId 为 null 或 0)
        Page<Comment> pageResult = commentService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .and(w -> w.isNull(Comment::getParentId).or().eq(Comment::getParentId, 0L))
                        .orderByDesc(Comment::getCreateTime)
        );
        
        List<Comment> comments = pageResult.getRecords();
        if (comments.isEmpty()) {
            return Result.success(PageResult.of(new ArrayList<>(), 0L, page, pageSize));
        }
        
        // 获取所有评论的子评论（包括多层嵌套的情况）
        List<Long> parentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> allReplies = new ArrayList<>();
        
        if (!parentIds.isEmpty()) {
            // 获取所有非顶级评论（parentId 不为 null 且不为 0）
            allReplies = commentService.list(
                    new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getArticleId, articleId)
                            .eq(Comment::getStatus, 1)
                            .isNotNull(Comment::getParentId)
                            .ne(Comment::getParentId, 0L)
                            .orderByAsc(Comment::getCreateTime)
            );
        }
        
        // 收集所有用户ID
        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        userIds.addAll(allReplies.stream().map(Comment::getUserId).collect(Collectors.toSet()));
        userIds.addAll(allReplies.stream()
                .filter(r -> r.getReplyUserId() != null)
                .map(Comment::getReplyUserId)
                .collect(Collectors.toSet()));
        
        // 批量查询用户信息
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // 构建评论ID到评论的映射，用于查找根评论
        Map<Long, Comment> commentMap = new java.util.HashMap<>();
        comments.forEach(c -> commentMap.put(c.getId(), c));
        allReplies.forEach(c -> commentMap.put(c.getId(), c));
        
        // 找到每个子评论的根评论ID
        Map<Long, List<Comment>> repliesMap = new java.util.HashMap<>();
        for (Comment reply : allReplies) {
            Long rootId = findRootCommentId(reply, commentMap, parentIds);
            if (rootId != null) {
                repliesMap.computeIfAbsent(rootId, k -> new ArrayList<>()).add(reply);
            }
        }
        
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
    
    // 递归查找根评论ID
    private Long findRootCommentId(Comment comment, Map<Long, Comment> commentMap, List<Long> rootIds) {
        Long parentId = comment.getParentId();
        if (parentId == null || parentId == 0L) {
            return comment.getId();
        }
        if (rootIds.contains(parentId)) {
            return parentId;
        }
        Comment parent = commentMap.get(parentId);
        if (parent != null) {
            return findRootCommentId(parent, commentMap, rootIds);
        }
        // 如果找不到父评论，直接返回parentId（可能是已删除的评论）
        return parentId;
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
            userInfo.setUserLevel(user.getUserLevel());
            userInfo.setVipLevel(user.getVipLevel());
            userInfo.setStatus(user.getStatus());
            vo.setUser(userInfo);
        }
        
        if (comment.getReplyUserId() != null) {
            User replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                CommentVO.UserInfo replyUserInfo = new CommentVO.UserInfo();
                replyUserInfo.setId(replyUser.getId());
                replyUserInfo.setNickname(replyUser.getNickname());
                replyUserInfo.setAvatar(replyUser.getAvatar());
                replyUserInfo.setUserLevel(replyUser.getUserLevel());
                replyUserInfo.setVipLevel(replyUser.getVipLevel());
                replyUserInfo.setStatus(replyUser.getStatus());
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
            
            // 发送通知
            if (request.getParentId() != null && request.getParentId() > 0) {
                // 回复评论 - 通知被回复的用户
                if (request.getReplyUserId() != null && !request.getReplyUserId().equals(userId)) {
                    notificationService.notifyCommentReplied(
                        request.getReplyUserId(), userId, article.getId(), 
                        article.getTitle(), request.getParentId(), request.getContent()
                    );
                }
            } else {
                // 新评论 - 通知文章作者
                if (!article.getUserId().equals(userId)) {
                    notificationService.notifyArticleCommented(
                        article.getUserId(), userId, article.getId(), 
                        article.getTitle(), request.getContent()
                    );
                }
            }
            
            // 广播新评论到文章页面（实时刷新评论区）
            broadcastNewComment(request.getArticleId(), comment, userId);
            redisService.clearArticleCache(article.getId());
            searchService.syncArticle(article.getId());
        }
        userExpAsyncService.publishGrant(
                userId,
                ExpConstants.BIZ_COMMENT_CREATE,
                "comment:" + comment.getId(),
                ExpConstants.EXP_COMMENT_CREATE,
                "发表评论获得经验",
                "CommentController.create"
        );

        // 返回新评论的ID
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", comment.getId());
        result.put("message", "评论成功");
        return Result.success(result);
    }
    
    // 广播新评论
    private void broadcastNewComment(Long articleId, Comment comment, Long userId) {
        try {
            User user = userService.getById(userId);
            Map<String, Object> commentData = new java.util.HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("articleId", articleId);
            commentData.put("content", comment.getContent());
            commentData.put("parentId", comment.getParentId());
            commentData.put("replyUserId", comment.getReplyUserId());
            commentData.put("createTime", comment.getCreateTime());
            
            if (user != null) {
                Map<String, Object> userInfo = new java.util.HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("userLevel", user.getUserLevel());
                userInfo.put("vipLevel", user.getVipLevel());
                userInfo.put("status", user.getStatus());
                commentData.put("user", userInfo);
            }
            
            // 如果有被回复用户
            if (comment.getReplyUserId() != null) {
                User replyUser = userService.getById(comment.getReplyUserId());
                if (replyUser != null) {
                    Map<String, Object> replyUserInfo = new java.util.HashMap<>();
                    replyUserInfo.put("id", replyUser.getId());
                    replyUserInfo.put("nickname", replyUser.getNickname());
                    replyUserInfo.put("userLevel", replyUser.getUserLevel());
                    replyUserInfo.put("status", replyUser.getStatus());
                    commentData.put("replyUser", replyUserInfo);
                }
            }
            
            // 通过WebSocket广播到所有在线用户
            webSocketServer.broadcastToArticle(articleId, "new_comment", commentData);
        } catch (Exception e) {
            // 广播失败不影响评论功能
        }
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
            return Result.error(ResultCode.FORBIDDEN, "只能删除自己的评论");
        }

        // 同时删除该评论的所有子评论
        List<Comment> childComments = commentService.list(
                new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, id)
        );
        int deleteCount = 1 + childComments.size();
        
        commentService.removeById(id);
        if (!childComments.isEmpty()) {
            commentService.removeByIds(childComments.stream().map(Comment::getId).collect(Collectors.toList()));
        }

        // 更新文章评论数
        Article article = articleService.getById(comment.getArticleId());
        if (article != null && article.getCommentCount() >= deleteCount) {
            article.setCommentCount(article.getCommentCount() - deleteCount);
            articleService.updateById(article);
            redisService.clearArticleCache(article.getId());
            searchService.syncArticle(article.getId());
        }

        return Result.success("删除成功");
    }

    @Operation(summary = "更新评论")
    @PutMapping("/comments/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }

        if (!comment.getUserId().equals(userId)) {
            return Result.error(ResultCode.FORBIDDEN, "只能编辑自己的评论");
        }

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "评论内容不能为空");
        }

        comment.setContent(content.trim());
        commentService.updateById(comment);

        return Result.success("修改成功");
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
