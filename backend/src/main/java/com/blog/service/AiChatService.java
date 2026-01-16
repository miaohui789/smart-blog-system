package com.blog.service;

import com.blog.entity.AiConfig;
import com.blog.entity.AiConversation;
import com.blog.entity.AiMessage;

import java.util.List;

public interface AiChatService {
    
    /**
     * 获取AI配置
     */
    AiConfig getConfig();
    
    /**
     * 更新AI配置
     */
    void updateConfig(AiConfig config);
    
    /**
     * 检查用户今日是否超过限制
     */
    boolean checkDailyLimit(Long userId);
    
    /**
     * 获取用户今日使用次数
     */
    int getTodayUsage(Long userId);
    
    /**
     * 创建新会话
     */
    AiConversation createConversation(Long userId, String title);
    
    /**
     * 获取用户会话列表
     */
    List<AiConversation> getConversations(Long userId);
    
    /**
     * 获取会话消息
     */
    List<AiMessage> getMessages(Long conversationId, Long userId);
    
    /**
     * 删除会话
     */
    void deleteConversation(Long conversationId, Long userId);
    
    /**
     * 更新会话标题
     */
    void updateConversationTitle(Long conversationId, Long userId, String title);
    
    /**
     * 发送消息并获取AI回复（流式）
     */
    void chat(Long userId, Long conversationId, String message, AiStreamCallback callback);
    
    /**
     * 流式回调接口
     */
    interface AiStreamCallback {
        void onStart();
        void onToken(String token);
        void onComplete(String fullResponse);
        void onError(String error);
    }
}
