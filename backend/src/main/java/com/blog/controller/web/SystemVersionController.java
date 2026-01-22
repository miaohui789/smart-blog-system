package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import com.blog.entity.SystemVersion;
import com.blog.service.SystemVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统版本")
@RestController
@RequestMapping("/api/system-version")
@RequiredArgsConstructor
public class SystemVersionController {

    private final SystemVersionService systemVersionService;

    @Operation(summary = "获取版本历史列表")
    @GetMapping("/list")
    public Result<?> list() {
        LambdaQueryWrapper<SystemVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemVersion::getStatus, 1)
               .orderByDesc(SystemVersion::getSortOrder)
               .orderByDesc(SystemVersion::getReleaseDate);
        
        List<SystemVersion> list = systemVersionService.list(wrapper);
        return Result.success(list);
    }
}
