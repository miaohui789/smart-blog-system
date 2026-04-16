package com.blog.service;

import com.blog.entity.AiConfig;
import com.blog.entity.AiConversation;
import com.blog.entity.AiMessage;

import java.util.List;

public interface AiChatService {
    
    /**
     * 获取AI配置（默认/第一条）
     */
    AiConfig getConfig();

    /**
     * 获取学习模块AI评分配置
     */
    AiConfig getStudyScoreConfig();
    
    /**
     * 根据ID获取AI配置
     */
    AiConfig getConfigById(Long configId);
    
    /**
     * 获取所有AI配置列表（管理端）
     */
    List<AiConfig> getAllConfigs();
    
    /**
     * 获取所有已启用的AI配置列表（用户端）
     */
    List<AiConfig> getEnabledConfigs();
    
    /**
     * 新增AI配置
     */
    void addConfig(AiConfig config);
    
    /**
     * 更新AI配置
     */
    void updateConfig(AiConfig config);
    
    /**
     * 删除AI配置
     */
    void deleteConfig(Long configId);
    
    /**
     * 设置默认模型
     */
    void setDefaultConfig(Long configId);

    /**
     * 设置学习模块AI评分默认模型
     */
    void setDefaultStudyScoreConfig(Long configId);
    
    /**
     * 获取用户当前选择的模型配置
     */
    AiConfig getUserModelConfig(Long userId);
    
    /**
     * 用户切换模型
     */
    void switchUserModel(Long userId, Long configId);
    
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
        void onKnowledge(String knowledgeJson);
        void onThinking(String thinkingToken);
        void onToken(String token);
        void onComplete(String fullResponse);
        void onError(String error);

        default boolean isCancelled() {
            return false;
        }
    }
}
