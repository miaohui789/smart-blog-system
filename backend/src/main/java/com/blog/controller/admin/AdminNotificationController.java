package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.entity.Notification;
import com.blog.entity.User;
import com.blog.service.NotificationService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "通知列表")
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer isRead) {
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(Notification::getUserId, userId);
        }
        
        if (StringUtils.hasText(type)) {
            wrapper.eq(Notification::getType, type);
        }
        
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        
        wrapper.orderByDesc(Notification::getCreateTime);
        
        Page<Notification> pageResult = notificationService.page(new Page<>(page, pageSize), wrapper);
        
        // 获取用户信息
        Set<Long> userIds = new HashSet<>();
        pageResult.getRecords().forEach(n -> {
            userIds.add(n.getUserId());
            if (n.getSenderId() != null) {
                userIds.add(n.getSenderId());
            }
        });
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("userId", n.getUserId());
            map.put("type", n.getType());
            map.put("title", n.getTitle());
            map.put("content", n.getContent());
            map.put("senderId", n.getSenderId());
            map.put("targetType", n.getTargetType());
            map.put("targetId", n.getTargetId());
            map.put("isRead", n.getIsRead());
            map.put("createTime", n.getCreateTime());
            
            User user = userMap.get(n.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                map.put("user", userInfo);
            }
            
            if (n.getSenderId() != null) {
                User sender = userMap.get(n.getSenderId());
                if (sender != null) {
                    Map<String, Object> senderInfo = new HashMap<>();
                    senderInfo.put("id", sender.getId());
                    senderInfo.put("nickname", sender.getNickname());
                    senderInfo.put("avatar", sender.getAvatar());
                    map.put("sender", senderInfo);
                }
            }
            
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    @Operation(summary = "发送系统通知")
    @PostMapping("/system")
    public Result<?> sendSystemNotification(@RequestBody Map<String, Object> data) {
        String title = (String) data.get("title");
        String content = (String) data.get("content");
        @SuppressWarnings("unchecked")
        List<Integer> userIds = (List<Integer>) data.get("userIds");
        Boolean sendToAll = (Boolean) data.get("sendToAll");
        
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            return Result.error("标题和内容不能为空");
        }
        
        if (Boolean.TRUE.equals(sendToAll)) {
            // 发送给所有用户
            List<User> allUsers = userService.list();
            for (User user : allUsers) {
                notificationService.createNotification(
                    user.getId(), "SYSTEM", title, content,
                    null, null, null, null
                );
            }
            return Result.success("已发送给 " + allUsers.size() + " 位用户");
        } else if (userIds != null && !userIds.isEmpty()) {
            // 发送给指定用户
            for (Integer userId : userIds) {
                notificationService.createNotification(
                    userId.longValue(), "SYSTEM", title, content,
                    null, null, null, null
                );
            }
            return Result.success("已发送给 " + userIds.size() + " 位用户");
        } else {
            return Result.error("请选择接收用户或选择发送给所有用户");
        }
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        notificationService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除通知")
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        notificationService.removeByIds(ids);
        return Result.success("删除成功");
    }

    @Operation(summary = "通知统计")
    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalNotifications", notificationService.count());
        
        // 未读通知数
        LambdaQueryWrapper<Notification> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Notification::getIsRead, 0);
        data.put("unreadNotifications", notificationService.count(unreadWrapper));
        
        // 各类型通知数量
        Map<String, Long> typeStats = new HashMap<>();
        String[] types = {"LIKE_ARTICLE", "FAVORITE_ARTICLE", "COMMENT", "REPLY", "FOLLOW", "SYSTEM"};
        for (String type : types) {
            LambdaQueryWrapper<Notification> typeWrapper = new LambdaQueryWrapper<>();
            typeWrapper.eq(Notification::getType, type);
            typeStats.put(type, notificationService.count(typeWrapper));
        }
        data.put("typeStats", typeStats);
        
        return Result.success(data);
    }

    @Operation(summary = "清空用户通知")
    @DeleteMapping("/user/{userId}")
    public Result<?> clearUserNotifications(@PathVariable Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        notificationService.remove(wrapper);
        return Result.success("清空成功");
    }
}
