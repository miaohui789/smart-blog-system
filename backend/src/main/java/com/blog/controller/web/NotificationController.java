package com.blog.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.enums.ResultCode;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.response.NotificationVO;
import com.blog.entity.Notification;
import com.blog.entity.User;
import com.blog.security.SecurityUser;
import com.blog.service.NotificationService;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "通知接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String type) {
        
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type.toUpperCase());
        }
        
        Page<Notification> pageResult = notificationService.page(new Page<>(page, pageSize), wrapper);
        
        List<Notification> notifications = pageResult.getRecords();
        if (notifications.isEmpty()) {
            return Result.success(PageResult.of(new ArrayList<>(), 0L, page, pageSize));
        }
        
        // 批量获取发送者信息
        Set<Long> senderIds = notifications.stream()
                .filter(n -> n.getSenderId() != null)
                .map(Notification::getSenderId)
                .collect(Collectors.toSet());
        
        Map<Long, User> userMap = senderIds.isEmpty() ? new HashMap<>() :
                userService.listByIds(senderIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        
        // 转换为VO
        List<NotificationVO> voList = notifications.stream().map(n -> {
            NotificationVO vo = new NotificationVO();
            vo.setId(n.getId());
            vo.setType(n.getType());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setTargetType(n.getTargetType());
            vo.setTargetId(n.getTargetId());
            vo.setIsRead(n.getIsRead());
            vo.setCreateTime(n.getCreateTime());
            
            if (n.getSenderId() != null) {
                User sender = userMap.get(n.getSenderId());
                if (sender != null) {
                    NotificationVO.SenderInfo senderInfo = new NotificationVO.SenderInfo();
                    senderInfo.setId(sender.getId());
                    senderInfo.setNickname(sender.getNickname());
                    senderInfo.setAvatar(sender.getAvatar());
                    senderInfo.setUserLevel(sender.getUserLevel());
                    senderInfo.setVipLevel(sender.getVipLevel());
                    vo.setSender(senderInfo);
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(PageResult.of(voList, pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<?> getUnreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        Map<String, Long> counts = notificationService.getUnreadCount(userId);
        return Result.success(counts);
    }

    @Operation(summary = "标记通知为已读")
    @PostMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        Notification notification = notificationService.getById(id);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        
        notification.setIsRead(1);
        notificationService.updateById(notification);
        
        return Result.success("已标记为已读");
    }

    @Operation(summary = "标记所有通知为已读")
    @PostMapping("/read-all")
    public Result<?> markAllAsRead(@RequestParam(required = false) String type) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        if (type != null && !type.isEmpty()) {
            notificationService.markTypeAsRead(userId, type.toUpperCase());
        } else {
            notificationService.markAllAsRead(userId);
        }
        
        return Result.success("已全部标记为已读");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        Notification notification = notificationService.getById(id);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        
        notificationService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "清空所有通知")
    @DeleteMapping("/clear")
    public Result<?> clearAll(@RequestParam(required = false) String type) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type.toUpperCase());
        }
        
        notificationService.remove(wrapper);
        return Result.success("清空成功");
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
