package com.blog.controller.web;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.response.UserExpRecordVO;
import com.blog.dto.response.UserExpSummaryVO;
import com.blog.dto.response.UserSignInCalendarVO;
import com.blog.dto.response.UserSignInResultVO;
import com.blog.service.UserExpService;
import com.blog.service.UserSignInService;
import com.blog.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户经验接口")
@RestController
@RequestMapping("/api/user/exp")
@RequiredArgsConstructor
public class UserExpController {

    private final UserExpService userExpService;
    private final UserSignInService userSignInService;

    @Operation(summary = "获取经验概览")
    @GetMapping("/summary")
    public Result<UserExpSummaryVO> getSummary() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(userExpService.getUserExpSummary(userId));
    }

    @Operation(summary = "获取经验流水")
    @GetMapping("/records")
    public Result<PageResult<UserExpRecordVO>> getRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(userExpService.getUserExpRecords(userId, page, pageSize));
    }

    @Operation(summary = "获取签到日历")
    @GetMapping("/sign/calendar")
    public Result<UserSignInCalendarVO> getSignInCalendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(userSignInService.getSignInCalendar(userId, year, month));
    }

    @Operation(summary = "今日签到")
    @PostMapping("/sign")
    public Result<UserSignInResultVO> signToday() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return Result.success(userSignInService.signToday(userId));
    }
}
