package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Message;
import com.blog.entity.MessageConversation;

import java.util.List;

public interface MessageService extends IService<Message> {
    
    /**
     * 发送私信
     */
    Message sendMessage(Long senderId, Long receiverId, String content, Integer type);
    
    /**
     * 检查是否可以发送消息（陌生人限制）
     * @return null表示可以发送，否则返回错误提示
     */
    String checkCanSendMessage(Long senderId, Long receiverId);
    
    /**
     * 获取会话列表
     */
    List<MessageConversation> getConversationList(Long userId);
    
    /**
     * 获取会话消息列表
     */
    List<Message> getMessageList(Long conversationId, Long userId, Integer page, Integer pageSize);
    
    /**
     * 标记消息已读
     */
    void markAsRead(Long conversationId, Long userId);
    
    /**
     * 获取未读消息总数
     */
    int getUnreadCount(Long userId);
    
    /**
     * 获取或创建会话
     */
    MessageConversation getOrCreateConversation(Long userId1, Long userId2);
    
    /**
     * 删除消息
     */
    boolean deleteMessage(Long messageId, Long userId);
    
    /**
     * 撤回消息（2分钟内）
     */
    boolean withdrawMessage(Long messageId, Long userId);
    
    /**
     * 删除会话
     */
    boolean deleteConversation(Long conversationId, Long userId);
}
