package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.entity.SystemVersion;
import com.blog.service.SystemVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-系统版本管理")
@RestController
@RequestMapping("/api/admin/system-version")
@RequiredArgsConstructor
public class AdminSystemVersionController {

    private final SystemVersionService systemVersionService;

    @Operation(summary = "分页查询版本列表")
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") Integer current,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String keyword) {
        Page<SystemVersion> page = new Page<>(current, size);
        LambdaQueryWrapper<SystemVersion> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(SystemVersion::getVersionNumber, keyword)
                .or()
                .like(SystemVersion::getVersionName, keyword)
            );
        }
        
        wrapper.orderByDesc(SystemVersion::getSortOrder)
               .orderByDesc(SystemVersion::getReleaseDate);
        
        return Result.success(systemVersionService.page(page, wrapper));
    }

    @Operation(summary = "获取版本详情")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(systemVersionService.getById(id));
    }

    @Operation(summary = "新增版本")
    @PostMapping
    public Result<?> add(@RequestBody SystemVersion systemVersion) {
        // 检查版本号是否已存在
        LambdaQueryWrapper<SystemVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemVersion::getVersionNumber, systemVersion.getVersionNumber());
        if (systemVersionService.count(wrapper) > 0) {
            return Result.error("版本号已存在");
        }
        
        systemVersionService.save(systemVersion);
        return Result.success("新增成功");
    }

    @Operation(summary = "更新版本")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SystemVersion systemVersion) {
        systemVersion.setId(id);
        
        // 检查版本号是否与其他版本重复
        LambdaQueryWrapper<SystemVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemVersion::getVersionNumber, systemVersion.getVersionNumber())
               .ne(SystemVersion::getId, id);
        if (systemVersionService.count(wrapper) > 0) {
            return Result.error("版本号已存在");
        }
        
        systemVersionService.updateById(systemVersion);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除版本")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        systemVersionService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除版本")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody java.util.List<Long> ids) {
        systemVersionService.removeByIds(ids);
        return Result.success("删除成功");
    }

    @Operation(summary = "更新版本状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SystemVersion systemVersion = new SystemVersion();
        systemVersion.setId(id);
        systemVersion.setStatus(status);
        systemVersionService.updateById(systemVersion);
        return Result.success("状态更新成功");
    }
}
