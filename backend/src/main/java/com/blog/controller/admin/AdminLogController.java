package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.entity.LoginLog;
import com.blog.entity.OperationLog;
import com.blog.service.LogService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "日志管理")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final LogService logService;
    private final com.blog.mapper.LoginLogMapper loginLogMapper;
    private final UserService userService;

    @Operation(summary = "操作日志列表")
    @GetMapping("/operation")
    public Result<?> operationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreateTime);
        
        Page<OperationLog> pageResult = logService.page(new Page<>(page, pageSize), wrapper);
        fillOperationLogUsername(pageResult.getRecords());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        
        return Result.success(result);
    }

    @Operation(summary = "登录日志列表")
    @GetMapping("/login")
    public Result<?> loginLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LoginLog::getCreateTime);
        
        Page<LoginLog> pageResult = loginLogMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        
        return Result.success(result);
    }

    @Operation(summary = "清空操作日志")
    @DeleteMapping("/operation/clean")
    public Result<?> cleanOperationLogs() {
        logService.remove(null);
        return Result.success("清空成功");
    }

    @Operation(summary = "清空登录日志")
    @DeleteMapping("/login/clean")
    public Result<?> cleanLoginLogs() {
        loginLogMapper.delete(null);
        return Result.success("清空成功");
    }

    private void fillOperationLogUsername(List<OperationLog> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> userIds = records.stream()
                .map(OperationLog::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, String> usernameMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(com.blog.entity.User::getId, com.blog.entity.User::getUsername));

        for (OperationLog record : records) {
            if (record.getUserId() != null) {
                record.setUsername(usernameMap.getOrDefault(record.getUserId(), "-"));
            }
        }
    }
}
