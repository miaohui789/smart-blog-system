package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final ArticleService articleService;

    @Operation(summary = "评论列表")
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Comment::getContent, keyword);
        }
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);

        Page<Comment> pageResult = commentService.page(new Page<>(page, pageSize), wrapper);
        
        // 填充用户信息和文章标题
        List<Map<String, Object>> records = pageResult.getRecords().stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("status", comment.getStatus());
            map.put("createTime", comment.getCreateTime());
            
            // 用户信息
            if (comment.getUserId() != null) {
                User user = userService.getById(comment.getUserId());
                if (user != null) {
                    map.put("nickname", user.getNickname());
                    map.put("avatar", user.getAvatar());
                }
            }
            if (!map.containsKey("nickname")) {
                map.put("nickname", "匿名用户");
                map.put("avatar", null);
            }
            
            // 文章标题
            if (comment.getArticleId() != null) {
                Article article = articleService.getById(comment.getArticleId());
                if (article != null) {
                    map.put("articleTitle", article.getTitle());
                }
            }
            
            return map;
        }).toList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", pageResult.getTotal());
        
        return Result.success(result);
    }

    @Operation(summary = "审核评论")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(body.get("status"));
        commentService.updateById(comment);
        return Result.success("操作成功");
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        commentService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除评论")
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids != null && !ids.isEmpty()) {
            commentService.removeByIds(ids);
        }
        return Result.success("删除成功");
    }
}
