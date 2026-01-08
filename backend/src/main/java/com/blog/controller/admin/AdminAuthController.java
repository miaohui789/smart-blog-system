package com.blog.controller.admin;

import com.blog.common.enums.ResultCode;
import com.blog.common.result.Result;
import com.blog.dto.request.LoginRequest;
import com.blog.entity.User;
import com.blog.security.JwtTokenProvider;
import com.blog.security.SecurityUser;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "管理端认证接口")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();
        
        // 检查是否有管理员权限（可以根据角色判断）
        // 这里简单判断，实际可以检查用户角色
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @Operation(summary = "获取管理员信息")
    @GetMapping("/info")
    public Result<?> info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User user = userService.getById(securityUser.getUser().getId());
            if (user != null) {
                user.setPassword(null);
                
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("nickname", user.getNickname());
                data.put("avatar", user.getAvatar());
                data.put("email", user.getEmail());
                data.put("roles", new String[]{"admin"});
                data.put("permissions", new String[]{"*:*:*"});
                
                return Result.success(data);
            }
        }
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtTokenProvider.invalidateToken(token);
        }
        return Result.success("退出成功");
    }
}
