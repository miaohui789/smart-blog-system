package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.dto.response.DashboardVO;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ArticleService articleService;
    private final CommentService commentService;
    private final UserService userService;

    @Operation(summary = "统计数据")
    @GetMapping("/stats")
    public Result<DashboardVO> stats() {
        DashboardVO vo = new DashboardVO();
        vo.setArticleCount(articleService.count());
        vo.setCommentCount(commentService.count());
        vo.setUserCount(userService.count());
        vo.setViewCount(0L);
        return Result.success(vo);
    }

    @Operation(summary = "访问趋势")
    @GetMapping("/visit-trend")
    public Result<?> visitTrend() {
        Map<String, Object> data = new HashMap<>();
        data.put("xAxis", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
        data.put("series", Arrays.asList(120, 200, 150, 80, 70, 110, 130));
        return Result.success(data);
    }

    @Operation(summary = "分类统计")
    @GetMapping("/category-stats")
    public Result<?> categoryStats() {
        return Result.success(Collections.emptyList());
    }
}
