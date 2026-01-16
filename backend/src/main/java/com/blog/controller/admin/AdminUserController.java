package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "用户列表")
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageResult = userService.page(new Page<>(page, pageSize), wrapper);
        
        // 构建包含角色信息的用户列表
        List<Map<String, Object>> userList = pageResult.getRecords().stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("email", user.getEmail());
            userMap.put("avatar", user.getAvatar());
            userMap.put("status", user.getStatus());
            userMap.put("createTime", user.getCreateTime());
            userMap.put("roles", userRoleService.getRoleNamesByUserId(user.getId()));
            userMap.put("roleIds", userRoleService.getRoleIdsByUserId(user.getId()));
            return userMap;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", userList);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> data) {
        String username = (String) data.get("username");
        // 检查用户名是否存在
        if (userService.getByUsername(username) != null) {
            return Result.error("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode((String) data.get("password")));
        user.setNickname((String) data.get("nickname"));
        user.setEmail((String) data.get("email"));
        user.setStatus(1);
        userService.save(user);
        
        // 保存用户角色关联
        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) data.get("roleIds");
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Long> roleIdList = roleIds.stream().map(Integer::longValue).collect(Collectors.toList());
            userRoleService.updateUserRoles(user.getId(), roleIdList);
        }
        
        return Result.success("创建成功");
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (data.get("nickname") != null) {
            user.setNickname((String) data.get("nickname"));
        }
        if (data.get("email") != null) {
            user.setEmail((String) data.get("email"));
        }
        userService.updateById(user);
        
        // 更新用户角色关联
        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) data.get("roleIds");
        if (roleIds != null) {
            List<Long> roleIdList = roleIds.stream().map(Integer::longValue).collect(Collectors.toList());
            userRoleService.updateUserRoles(id, roleIdList);
        }
        
        return Result.success("更新成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "注销用户")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 设置用户状态为已注销(2)
        user.setStatus(2);
        userService.updateById(user);
        return Result.success("用户已注销");
    }

    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = new User();
        user.setId(id);
        user.setStatus(body.get("status"));
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(user);
        return Result.success("密码已重置为 123456");
    }
}
