package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.AiConfig;
import com.blog.entity.AiConversation;
import com.blog.entity.AiMessage;
import com.blog.entity.AiUsage;
import com.blog.mapper.AiConfigMapper;
import com.blog.mapper.AiConversationMapper;
import com.blog.mapper.AiMessageMapper;
import com.blog.mapper.AiUsageMapper;
import com.blog.service.AiChatService;
import com.blog.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiConfigMapper configMapper;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiUsageMapper usageMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    private static final String CACHE_AI_CONFIG = "blog:ai:config";
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public AiConfig getConfig() {
        // 先从缓存获取
        Object cached = redisService.get(CACHE_AI_CONFIG);
        if (cached instanceof AiConfig) {
            return (AiConfig) cached;
        }
        
        AiConfig config = configMapper.selectOne(new LambdaQueryWrapper<AiConfig>().last("LIMIT 1"));
        if (config != null) {
            redisService.setWithMinutes(CACHE_AI_CONFIG, config, RedisService.EXPIRE_LONG);
        }
        return config;
    }

    @Override
    public void updateConfig(AiConfig config) {
        if (config.getId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
        redisService.delete(CACHE_AI_CONFIG);
    }

    @Override
    public boolean checkDailyLimit(Long userId) {
        AiConfig config = getConfig();
        if (config == null || config.getEnabled() != 1) {
            return false;
        }
        
        int todayUsage = getTodayUsage(userId);
        return todayUsage < config.getDailyLimit();
    }

    @Override
    public int getTodayUsage(Long userId) {
        AiUsage usage = usageMapper.selectOne(new LambdaQueryWrapper<AiUsage>()
                .eq(AiUsage::getUserId, userId)
                .eq(AiUsage::getDate, LocalDate.now()));
        return usage != null ? usage.getRequestCount() : 0;
    }

    @Override
    public AiConversation createConversation(Long userId, String title) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "新对话");
        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public List<AiConversation> getConversations(Long userId) {
        return conversationMapper.selectList(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getUpdateTime));
    }

    @Override
    public List<AiMessage> getMessages(Long conversationId, Long userId) {
        // 验证会话属于该用户
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return new ArrayList<>();
        }
        
        return messageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getCreateTime));
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null && conversation.getUserId().equals(userId)) {
            // 删除消息
            messageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                    .eq(AiMessage::getConversationId, conversationId));
            // 删除会话
            conversationMapper.deleteById(conversationId);
        }
    }

    @Override
    public void updateConversationTitle(Long conversationId, Long userId, String title) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null && conversation.getUserId().equals(userId)) {
            conversation.setTitle(title);
            conversationMapper.updateById(conversation);
        }
    }

    @Override
    public void chat(Long userId, Long conversationId, String message, AiStreamCallback callback) {
        AiConfig config = getConfig();
        if (config == null || config.getEnabled() != 1) {
            callback.onError("AI功能未启用");
            return;
        }
        
        if (!checkDailyLimit(userId)) {
            callback.onError("今日使用次数已达上限");
            return;
        }
        
        // 验证会话
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            callback.onError("会话不存在");
            return;
        }
        
        // 保存用户消息
        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(message);
        userMessage.setTokens(0);
        messageMapper.insert(userMessage);
        
        // 更新会话标题（如果是第一条消息）
        if ("新对话".equals(conversation.getTitle())) {
            conversation.setTitle(message.length() > 20 ? message.substring(0, 20) + "..." : message);
            conversationMapper.updateById(conversation);
        }
        
        // 获取历史消息构建上下文
        List<AiMessage> history = getMessages(conversationId, userId);
        
        // 调用AI API
        callAiApi(config, history, callback, conversationId, userId);
    }
    
    private void callAiApi(AiConfig config, List<AiMessage> history, AiStreamCallback callback, 
                          Long conversationId, Long userId) {
        try {
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统提示
            if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", config.getSystemPrompt());
                messages.add(systemMsg);
            }
            
            // 历史消息（最多保留最近10轮对话）
            int startIdx = Math.max(0, history.size() - 20);
            for (int i = startIdx; i < history.size(); i++) {
                AiMessage msg = history.get(i);
                Map<String, String> m = new HashMap<>();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent());
                messages.add(m);
            }
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", config.getMaxTokens());
            requestBody.put("temperature", config.getTemperature().doubleValue());
            requestBody.put("stream", true);
            
            String json = objectMapper.writeValueAsString(requestBody);
            
            String baseUrl = config.getBaseUrl();
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "https://api.deepseek.com";
            }
            
            Request request = new Request.Builder()
                    .url(baseUrl + "/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();
            
            callback.onStart();
            
            StringBuilder fullResponse = new StringBuilder();
            
            EventSource.Factory factory = EventSources.createFactory(httpClient);
            factory.newEventSource(request, new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if ("[DONE]".equals(data)) {
                        // 保存AI回复
                        saveAiResponse(conversationId, userId, fullResponse.toString());
                        callback.onComplete(fullResponse.toString());
                        return;
                    }
                    
                    try {
                        JsonNode node = objectMapper.readTree(data);
                        JsonNode choices = node.get("choices");
                        if (choices != null && choices.isArray() && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                String content = delta.get("content").asText();
                                fullResponse.append(content);
                                callback.onToken(content);
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析AI响应失败: {}", e.getMessage());
                    }
                }
                
                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    String error = "AI服务请求失败";
                    if (response != null) {
                        try {
                            error = response.body() != null ? response.body().string() : "未知错误";
                        } catch (Exception e) {
                            error = "读取错误响应失败";
                        }
                    } else if (t != null) {
                        error = t.getMessage();
                    }
                    log.error("AI API调用失败: {}", error);
                    callback.onError(error);
                }
            });
            
        } catch (Exception e) {
            log.error("调用AI API异常: {}", e.getMessage());
            callback.onError("AI服务异常: " + e.getMessage());
        }
    }
    
    private void saveAiResponse(Long conversationId, Long userId, String content) {
        AiMessage aiMessage = new AiMessage();
        aiMessage.setConversationId(conversationId);
        aiMessage.setUserId(userId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(content);
        aiMessage.setTokens(content.length() / 4); // 粗略估算token数
        messageMapper.insert(aiMessage);
        
        // 更新使用统计
        usageMapper.incrementUsage(userId, LocalDate.now(), aiMessage.getTokens());
    }
}
