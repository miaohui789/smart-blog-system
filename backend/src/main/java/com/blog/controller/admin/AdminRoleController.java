package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import com.blog.entity.Role;
import com.blog.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表")
    @GetMapping
    public Result<List<Role>> list() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getSort);
        return Result.success(roleService.list(wrapper));
    }

    @Operation(summary = "所有角色")
    @GetMapping("/all")
    public Result<List<Role>> all() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1).orderByAsc(Role::getSort);
        return Result.success(roleService.list(wrapper));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<?> create(@RequestBody Role role) {
        role.setStatus(1);
        roleService.save(role);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateById(role);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success("删除成功");
    }
}
