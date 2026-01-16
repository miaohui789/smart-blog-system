package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Notification;

import java.util.Map;

public interface NotificationService extends IService<Notification> {
    
    /**
     * 创建通知并推送
     */
    void createNotification(Long userId, String type, String title, String content, 
                           Long senderId, String targetType, Long targetId, String extraData);
    
    /**
     * 文章被点赞通知
     */
    void notifyArticleLiked(Long articleUserId, Long senderId, Long articleId, String articleTitle);
    
    /**
     * 文章被收藏通知
     */
    void notifyArticleFavorited(Long articleUserId, Long senderId, Long articleId, String articleTitle);
    
    /**
     * 文章被评论通知
     */
    void notifyArticleCommented(Long articleUserId, Long senderId, Long articleId, String articleTitle, String commentContent);
    
    /**
     * 评论被回复通知
     */
    void notifyCommentReplied(Long commentUserId, Long senderId, Long articleId, String articleTitle, Long commentId, String replyContent);
    
    /**
     * 被关注通知
     */
    void notifyFollowed(Long followedUserId, Long senderId);
    
    /**
     * 获取未读通知数量
     */
    Map<String, Long> getUnreadCount(Long userId);
    
    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 标记指定类型通知为已读
     */
    void markTypeAsRead(Long userId, String type);
}
