package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Notification;
import com.blog.entity.User;
import com.blog.mapper.NotificationMapper;
import com.blog.service.NotificationService;
import com.blog.service.RedisService;
import com.blog.service.UserService;
import com.blog.websocket.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final WebSocketServer webSocketServer;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    @Override
    public void createNotification(Long userId, String type, String title, String content,
                                   Long senderId, String targetType, Long targetId, String extraData) {
        // 不给自己发通知
        if (userId.equals(senderId)) {
            return;
        }
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSenderId(senderId);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setExtraData(extraData);
        notification.setIsRead(0);
        
        this.save(notification);
        
        // 清除该用户的未读数缓存
        redisService.clearNotificationCache(userId);
        
        // 实时推送通知
        pushNotification(userId, notification);
    }

    private void pushNotification(Long userId, Notification notification) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", notification.getId());
            data.put("type", notification.getType());
            data.put("title", notification.getTitle());
            data.put("content", notification.getContent());
            data.put("targetType", notification.getTargetType());
            data.put("targetId", notification.getTargetId());
            data.put("createTime", notification.getCreateTime());
            
            // 获取发送者信息
            if (notification.getSenderId() != null) {
                User sender = userService.getById(notification.getSenderId());
                if (sender != null) {
                    Map<String, Object> senderInfo = new HashMap<>();
                    senderInfo.put("id", sender.getId());
                    senderInfo.put("nickname", sender.getNickname());
                    senderInfo.put("avatar", sender.getAvatar());
                    senderInfo.put("userLevel", sender.getUserLevel());
                    senderInfo.put("vipLevel", sender.getVipLevel());
                    data.put("sender", senderInfo);
                }
            }
            
            // 解析额外数据
            if (notification.getExtraData() != null) {
                try {
                    data.put("extraData", objectMapper.readValue(notification.getExtraData(), Map.class));
                } catch (Exception e) {
                    data.put("extraData", notification.getExtraData());
                }
            }
            
            webSocketServer.sendNotification(userId, "notification", data);
        } catch (Exception e) {
            log.error("推送通知失败: {}", e.getMessage());
        }
    }

    @Override
    public void notifyArticleLiked(Long articleUserId, Long senderId, Long articleId, String articleTitle) {
        User sender = userService.getById(senderId);
        String senderName = sender != null ? sender.getNickname() : "有人";
        String content = senderName + " 点赞了你的文章《" + truncate(articleTitle, 20) + "》";
        
        createNotification(articleUserId, "LIKE_ARTICLE", "文章获得点赞", content,
                senderId, "article", articleId, null);
    }

    @Override
    public void notifyArticleFavorited(Long articleUserId, Long senderId, Long articleId, String articleTitle) {
        User sender = userService.getById(senderId);
        String senderName = sender != null ? sender.getNickname() : "有人";
        String content = senderName + " 收藏了你的文章《" + truncate(articleTitle, 20) + "》";
        
        createNotification(articleUserId, "FAVORITE_ARTICLE", "文章被收藏", content,
                senderId, "article", articleId, null);
    }

    @Override
    public void notifyArticleCommented(Long articleUserId, Long senderId, Long articleId, 
                                       String articleTitle, String commentContent) {
        User sender = userService.getById(senderId);
        String senderName = sender != null ? sender.getNickname() : "有人";
        String content = senderName + " 评论了你的文章《" + truncate(articleTitle, 20) + "》：" 
                + truncate(commentContent, 30);
        
        try {
            Map<String, String> extra = new HashMap<>();
            extra.put("commentContent", commentContent);
            String extraJson = objectMapper.writeValueAsString(extra);
            
            createNotification(articleUserId, "COMMENT", "文章收到评论", content,
                    senderId, "article", articleId, extraJson);
        } catch (Exception e) {
            createNotification(articleUserId, "COMMENT", "文章收到评论", content,
                    senderId, "article", articleId, null);
        }
    }

    @Override
    public void notifyCommentReplied(Long commentUserId, Long senderId, Long articleId, 
                                     String articleTitle, Long commentId, String replyContent) {
        User sender = userService.getById(senderId);
        String senderName = sender != null ? sender.getNickname() : "有人";
        String content = senderName + " 回复了你在《" + truncate(articleTitle, 15) + "》的评论：" 
                + truncate(replyContent, 30);
        
        try {
            Map<String, Object> extra = new HashMap<>();
            extra.put("replyContent", replyContent);
            extra.put("articleTitle", articleTitle);
            String extraJson = objectMapper.writeValueAsString(extra);
            
            createNotification(commentUserId, "REPLY", "评论收到回复", content,
                    senderId, "article", articleId, extraJson);
        } catch (Exception e) {
            createNotification(commentUserId, "REPLY", "评论收到回复", content,
                    senderId, "article", articleId, null);
        }
    }

    @Override
    public void notifyFollowed(Long followedUserId, Long senderId) {
        User sender = userService.getById(senderId);
        String senderName = sender != null ? sender.getNickname() : "有人";
        String content = senderName + " 关注了你";
        
        createNotification(followedUserId, "FOLLOW", "新增粉丝", content,
                senderId, null, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Long> getUnreadCount(Long userId) {
        // 先从缓存获取
        String cacheKey = RedisService.CACHE_NOTIFICATION_UNREAD + userId;
        Object cached = redisService.get(cacheKey);
        if (cached instanceof Map) {
            return (Map<String, Long>) cached;
        }
        
        Map<String, Long> counts = new HashMap<>();
        
        // 总未读数
        long total = this.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        counts.put("total", total);
        
        // 各类型未读数
        String[] types = {"LIKE_ARTICLE", "FAVORITE_ARTICLE", "COMMENT", "REPLY", "FOLLOW", "SYSTEM"};
        for (String type : types) {
            long count = this.count(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getUserId, userId)
                    .eq(Notification::getType, type)
                    .eq(Notification::getIsRead, 0));
            counts.put(type.toLowerCase(), count);
        }
        
        // 缓存5分钟
        redisService.setWithMinutes(cacheKey, counts, RedisService.EXPIRE_SHORT);
        
        return counts;
    }

    @Override
    public void markAllAsRead(Long userId) {
        baseMapper.markAllAsRead(userId);
        // 清除缓存
        redisService.clearNotificationCache(userId);
    }

    @Override
    public void markTypeAsRead(Long userId, String type) {
        baseMapper.markTypeAsRead(userId, type);
        // 清除缓存
        redisService.clearNotificationCache(userId);
    }
    
    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }
}
