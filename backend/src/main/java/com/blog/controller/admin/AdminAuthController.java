package com.blog.controller.admin;

import com.blog.common.enums.ResultCode;
import com.blog.common.result.Result;
import com.blog.dto.request.LoginRequest;
import com.blog.entity.LoginLog;
import com.blog.entity.User;
import com.blog.security.JwtTokenProvider;
import com.blog.security.SecurityUser;
import com.blog.service.LogService;
import com.blog.service.UserService;
import com.blog.service.UserRoleService;
import com.blog.service.MenuService;
import com.blog.utils.IpUtils;
import com.blog.utils.LogRecordUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Tag(name = "管理端认证接口")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final MenuService menuService;
    private final LogService logService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        // 先检查用户是否存在以及状态
        User existingUser = userService.getByUsername(request.getUsername());
        if (existingUser != null && existingUser.getStatus() != null && existingUser.getStatus() == 2) {
            saveLoginLog(httpServletRequest, existingUser.getId(), request.getUsername(), 0, "该账号已注销");
            return Result.error("该账号已注销");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User user = securityUser.getUser();

            // 检查用户是否有管理员权限且角色未被禁用
            if (!hasValidAdminRole(user.getId())) {
                saveLoginLog(httpServletRequest, user.getId(), request.getUsername(), 0, ResultCode.NO_PERMISSION_OR_FROZEN.getMessage());
                return Result.error(ResultCode.NO_PERMISSION_OR_FROZEN);
            }

            String token = jwtTokenProvider.generateAdminToken(user.getId(), user.getUsername());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            saveLoginLog(httpServletRequest, user.getId(), request.getUsername(), 1, "登录成功");
            return Result.success(data);
        } catch (BadCredentialsException e) {
            saveLoginLog(httpServletRequest, existingUser != null ? existingUser.getId() : null, request.getUsername(), 0, ResultCode.PASSWORD_ERROR.getMessage());
            throw e;
        }
    }
    
    /**
     * 检查用户是否有有效的管理员角色
     * 规则：
     * 1. 用户必须拥有管理员角色（roleKey = 'admin' 或 'editor'）
     * 2. 该角色必须是启用状态（status = 1）
     */
    private boolean hasValidAdminRole(Long userId) {
        List<Map<String, Object>> roles = userRoleService.getRolesWithStatusByUserId(userId);
        
        for (Map<String, Object> role : roles) {
            String roleKey = (String) role.get("role_key");
            Object statusObj = role.get("status");
            int status = statusObj instanceof Number ? ((Number) statusObj).intValue() : 0;
            
            // 检查是否有管理员角色（admin或editor）且角色状态为启用
            if (("admin".equals(roleKey) || "editor".equals(roleKey)) && status == 1) {
                return true;
            }
        }
        return false;
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
                
                // 获取用户真实角色
                List<String> roles = userRoleService.getRoleNamesByUserId(user.getId());
                
                // 根据角色设置权限
                String[] permissions;
                if (roles.contains("超级管理员") || roles.contains("admin")) {
                    permissions = new String[]{"*:*:*"};
                } else if (roles.contains("内容编辑")) {
                    permissions = new String[]{"article:*:*", "category:*:*", "tag:*:*", "comment:*:*"};
                } else {
                    permissions = new String[]{};
                }
                
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("nickname", user.getNickname());
                data.put("avatar", user.getAvatar());
                data.put("email", user.getEmail());
                data.put("roles", roles.toArray(new String[0]));
                data.put("permissions", permissions);
                
                return Result.success(data);
            }
        }
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "获取用户菜单")
    @GetMapping("/menus")
    public Result<?> getMenus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Long userId = securityUser.getUser().getId();
            
            // 获取用户角色
            List<String> roles = userRoleService.getRoleNamesByUserId(userId);
            
            // 根据角色返回不同的菜单
            List<Map<String, Object>> menus = new ArrayList<>();
            
            if (roles.contains("超级管理员") || roles.contains("admin")) {
                // 超级管理员：返回所有菜单
                menus = getFullMenus();
            } else if (roles.contains("内容编辑")) {
                // 内容编辑：只返回仪表盘和文章管理相关菜单
                menus = getEditorMenus();
            } else {
                // 其他角色：只返回仪表盘
                menus = getDashboardOnly();
            }
            
            return Result.success(menus);
        }
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    // 获取完整菜单（超级管理员）
    private List<Map<String, Object>> getFullMenus() {
        List<Map<String, Object>> menus = new ArrayList<>();
        
        // 仪表盘
        menus.add(createMenu("Dashboard", "/dashboard", "Dashboard/index", "Odometer", null));
        
        // 文章管理
        Map<String, Object> articleMenu = createMenu("ArticleManage", "/article", null, "Document", Arrays.asList(
            createMenu("ArticleList", "/article/list", "Article/List", "List", null),
            createMenu("ArticleCreate", "/article/create", "Article/Edit", "Edit", null),
            createMenu("ArticleEdit", "/article/edit/:id", "Article/Edit", "Edit", null)
        ));
        articleMenu.put("redirect", "/article/list");
        menus.add(articleMenu);
        
        // 分类管理
        menus.add(createMenu("Category", "/category", "Category/index", "Folder", null));
        
        // 标签管理
        menus.add(createMenu("Tag", "/tag", "Tag/index", "PriceTag", null));
        
        // 评论管理
        menus.add(createMenu("Comment", "/comment", "Comment/index", "ChatDotRound", null));
        
        // 用户管理
        menus.add(createMenu("User", "/user", "User/index", "User", null));
        
        // 系统管理
        Map<String, Object> systemMenu = createMenu("System", "/system", null, "Setting", Arrays.asList(
            createMenu("Role", "/system/role", "System/Role/index", "Avatar", null),
            createMenu("Menu", "/system/menu", "System/Menu/index", "Menu", null),
            createMenu("Config", "/system/config", "System/Config/index", "Tools", null),
            createMenu("SystemVersion", "/system/version", "System/Version", "Histogram", null),
            createMenu("Log", "/system/log", "System/Log/index", "Tickets", null)
        ));
        systemMenu.put("redirect", "/system/role");
        menus.add(systemMenu);
        
        return menus;
    }

    // 获取内容编辑菜单
    private List<Map<String, Object>> getEditorMenus() {
        List<Map<String, Object>> menus = new ArrayList<>();
        
        // 仪表盘
        menus.add(createMenu("Dashboard", "/dashboard", "Dashboard/index", "Odometer", null));
        
        // 文章管理
        Map<String, Object> articleMenu = createMenu("ArticleManage", "/article", null, "Document", Arrays.asList(
            createMenu("ArticleList", "/article/list", "Article/List", "List", null),
            createMenu("ArticleCreate", "/article/create", "Article/Edit", "Edit", null),
            createMenu("ArticleEdit", "/article/edit/:id", "Article/Edit", "Edit", null)
        ));
        articleMenu.put("redirect", "/article/list");
        menus.add(articleMenu);
        
        // 分类管理
        menus.add(createMenu("Category", "/category", "Category/index", "Folder", null));
        
        // 标签管理
        menus.add(createMenu("Tag", "/tag", "Tag/index", "PriceTag", null));
        
        // 评论管理
        menus.add(createMenu("Comment", "/comment", "Comment/index", "ChatDotRound", null));
        
        return menus;
    }

    // 只返回仪表盘
    private List<Map<String, Object>> getDashboardOnly() {
        List<Map<String, Object>> menus = new ArrayList<>();
        menus.add(createMenu("Dashboard", "/dashboard", "Dashboard/index", "Odometer", null));
        return menus;
    }

    // 创建菜单项
    private Map<String, Object> createMenu(String name, String path, String component, String icon, List<Map<String, Object>> children) {
        Map<String, Object> menu = new HashMap<>();
        menu.put("name", name);
        menu.put("path", path);
        if (component != null) {
            menu.put("component", component);
        }
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("title", getMenuTitle(name));
        meta.put("icon", icon);
        menu.put("meta", meta);
        
        if (children != null && !children.isEmpty()) {
            menu.put("children", children);
        }
        
        return menu;
    }

    // 获取菜单标题
    private String getMenuTitle(String name) {
        Map<String, String> titles = new HashMap<>();
        titles.put("Dashboard", "仪表盘");
        titles.put("ArticleManage", "文章管理");
        titles.put("ArticleList", "文章列表");
        titles.put("ArticleCreate", "创建文章");
        titles.put("ArticleEdit", "编辑文章");
        titles.put("Category", "分类管理");
        titles.put("Tag", "标签管理");
        titles.put("Comment", "评论管理");
        titles.put("User", "用户管理");
        titles.put("System", "系统管理");
        titles.put("Role", "角色管理");
        titles.put("Menu", "菜单管理");
        titles.put("Config", "系统配置");
        titles.put("SystemVersion", "版本管理");
        titles.put("Log", "操作日志");
        return titles.getOrDefault(name, name);
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

    private void saveLoginLog(HttpServletRequest request, Long userId, String username, Integer status, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setStatus(status);
        loginLog.setMessage(message);

        String ip = IpUtils.getIpAddress(request);
        loginLog.setIp(ip);
        loginLog.setIpSource(LogRecordUtils.getIpSource(ip));

        String userAgent = request.getHeader("User-Agent");
        loginLog.setBrowser(LogRecordUtils.getBrowser(userAgent));
        loginLog.setOs(LogRecordUtils.getOs(userAgent));

        logService.saveLoginLog(loginLog);
    }
}
