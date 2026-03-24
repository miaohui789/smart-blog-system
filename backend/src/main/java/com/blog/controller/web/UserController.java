package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.enums.ResultCode;
import com.blog.common.result.Result;
import com.blog.dto.request.LoginRequest;
import com.blog.dto.request.RegisterRequest;
import com.blog.dto.request.ResetPasswordRequest;
import com.blog.entity.Role;
import com.blog.entity.User;
import com.blog.entity.UserRole;
import com.blog.security.JwtTokenProvider;
import com.blog.security.SecurityUser;
import com.blog.service.EmailService;
import com.blog.service.RoleService;
import com.blog.service.UserRoleService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Email;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "认证接口")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginRequest request) {
        // 先检查用户是否存在以及状态（支持用户名或邮箱）
        User existingUser = userService.getByUsernameOrEmail(request.getUsername());
        if (existingUser != null && existingUser.getStatus() != null && existingUser.getStatus() == 2) {
            return Result.error("该账号已注销");
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();
        
        // 检查用户是否有普通用户权限且角色未被禁用
        if (!hasValidUserRole(user.getId())) {
            return Result.error(ResultCode.NO_PERMISSION_OR_FROZEN);
        }
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }
    
    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/login/email")
    public Result<?> loginByEmail(@RequestParam String email,
                                   @RequestParam String code) {
        // 去除前后空格
        email = email != null ? email.trim() : "";
        code = code != null ? code.trim() : "";
        
        log.info("邮箱验证码登录请求 - email: {}, code: {}", email, code);
        
        // 验证验证码
        if (!emailService.verifyLoginCode(email, code)) {
            return Result.error("验证码错误或已过期");
        }
        
        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userService.getOne(wrapper);
        
        if (user == null) {
            return Result.error("该邮箱未注册");
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 2) {
            return Result.error("该账号已注销");
        }
        
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("该账号已被冻结");
        }
        
        // 检查用户是否有普通用户权限且角色未被禁用
        if (!hasValidUserRole(user.getId())) {
            return Result.error(ResultCode.NO_PERMISSION_OR_FROZEN);
        }
        
        // 生成token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }
    
    @Operation(summary = "发送登录验证码")
    @PostMapping("/login/send-code")
    public Result<?> sendLoginCode(@RequestParam @Email(message = "邮箱格式不正确") String email) {
        // 检查邮箱是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userService.getOne(wrapper);
        if (user == null) {
            return Result.error("该邮箱未注册");
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 2) {
            return Result.error("该账号已注销");
        }
        
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("该账号已被冻结");
        }
        
        boolean success = emailService.sendLoginCode(email);
        if (success) {
            return Result.success("验证码已发送");
        } else {
            return Result.error("发送过于频繁，请稍后再试");
        }
    }
    
    /**
     * 检查用户是否有有效的用户端登录权限
     * 规则：
     * 1. 超级管理员（roleKey = 'admin'）可以登录用户端
     * 2. 内容编辑（roleKey = 'editor'）可以登录用户端
     * 3. 普通用户（roleKey = 'user'）可以登录用户端
     * 4. 对应角色必须是启用状态（status = 1）
     */
    private boolean hasValidUserRole(Long userId) {
        List<Map<String, Object>> roles = userRoleService.getRolesWithStatusByUserId(userId);
        
        for (Map<String, Object> role : roles) {
            String roleKey = (String) role.get("role_key");
            Object statusObj = role.get("status");
            int status = statusObj instanceof Number ? ((Number) statusObj).intValue() : 0;
            
            // 检查是否有有效角色且角色状态为启用
            // admin（超级管理员）、editor（内容编辑）、user（普通用户）都可以登录用户端
            if (("admin".equals(roleKey) || "editor".equals(roleKey) || "user".equals(roleKey)) && status == 1) {
                return true;
            }
        }
        return false;
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestParam @Email(message = "邮箱格式不正确") String email) {
        // 检查邮箱是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (userService.getOne(wrapper) != null) {
            return Result.error("该邮箱已被注册");
        }
        
        boolean success = emailService.sendVerifyCode(email);
        if (success) {
            return Result.success("验证码已发送");
        } else {
            return Result.error("发送过于频繁，请稍后再试");
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest request) {
        // 验证验证码
        if (!emailService.verifyCode(request.getEmail(), request.getCode())) {
            return Result.error("验证码错误或已过期");
        }
        
        if (userService.getByUsername(request.getUsername()) != null) {
            return Result.error(ResultCode.USERNAME_EXIST);
        }
        
        // 检查邮箱是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userService.getOne(wrapper) != null) {
            return Result.error("该邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getUsername());
        user.setStatus(1);
        userService.save(user);

        // 为新用户分配"普通用户"角色
        assignDefaultRole(user.getId());

        return Result.success("注册成功");
    }

    /**
     * 为用户分配默认角色（普通用户）
     */
    private void assignDefaultRole(Long userId) {
        // 查找"普通用户"角色（roleKey = 'user'）
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleKey, "user").eq(Role::getStatus, 1);
        Role userRole = roleService.getOne(wrapper);
        
        if (userRole != null) {
            // 创建用户角色关联
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(userRole.getId());
            userRoleService.save(ur);
        }
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<?> info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            // 从数据库获取最新的用户信息
            User user = userService.getById(securityUser.getUser().getId());
            if (user != null) {
                // 不返回密码
                user.setPassword(null);
                return Result.success(user);
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

    @Operation(summary = "发送重置密码验证码")
    @PostMapping("/forgot-password/send-code")
    public Result<?> sendResetCode(@RequestParam @Email(message = "邮箱格式不正确") String email) {
        // 检查邮箱是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userService.getOne(wrapper);
        if (user == null) {
            return Result.error("该邮箱未注册");
        }
        
        boolean success = emailService.sendResetPasswordCode(email);
        if (success) {
            return Result.success("验证码已发送");
        } else {
            return Result.error("发送过于频繁，请稍后再试");
        }
    }

    @Operation(summary = "重置密码")
    @PostMapping("/forgot-password/reset")
    public Result<?> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        // 验证验证码
        if (!emailService.verifyResetCode(request.getEmail(), request.getCode())) {
            return Result.error("验证码错误或已过期");
        }
        
        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        User user = userService.getOne(wrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userService.updateById(user);
        
        return Result.success("密码重置成功");
    }
}
