package com.blog.controller.web;

import com.blog.common.enums.ResultCode;
import com.blog.common.result.Result;
import com.blog.dto.request.LoginRequest;
import com.blog.dto.request.RegisterRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(securityUser.getUser().getId(), securityUser.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest request) {
        if (userService.getByUsername(request.getUsername()) != null) {
            return Result.error(ResultCode.USERNAME_EXIST);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getUsername());
        user.setStatus(1);
        userService.save(user);

        return Result.success("注册成功");
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<?> info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return Result.success(securityUser.getUser());
        }
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("退出成功");
    }
}
