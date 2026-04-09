package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.entity.User;
import com.blog.security.SecurityUser;
import com.blog.service.SearchService;
import com.blog.service.UserService;
import com.blog.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理员个人中心")
@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final SearchService searchService;

    @Operation(summary = "获取个人信息")
    @GetMapping
    public Result<?> getProfile() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 获取用户真实角色
        List<String> roles = userRoleService.getRoleNamesByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("intro", user.getIntro());
        result.put("createTime", user.getCreateTime());
        result.put("loginTime", user.getLoginTime());
        result.put("roles", roles.toArray(new String[0]));
        
        return Result.success(result);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping
    public Result<?> updateProfile(@RequestBody Map<String, String> data) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (data.get("nickname") != null) {
            user.setNickname(data.get("nickname"));
        }
        if (data.get("email") != null) {
            user.setEmail(data.get("email"));
        }
        if (data.get("intro") != null) {
            user.setIntro(data.get("intro"));
        }
        if (data.get("avatar") != null) {
            user.setAvatar(data.get("avatar"));
        }
        
        userService.updateById(user);
        searchService.syncUser(userId);
        searchService.syncArticlesByUserId(userId);
        return Result.success("更新成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> data) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        String oldPassword = data.get("oldPassword");
        String newPassword = data.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        
        return Result.success("密码修改成功");
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
