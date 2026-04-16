package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.AiConfig;
import com.blog.entity.AiConversation;
import com.blog.entity.AiMessage;
import com.blog.entity.AiUsage;
import com.blog.entity.AiUserModel;
import com.blog.mapper.AiConfigMapper;
import com.blog.mapper.AiConversationMapper;
import com.blog.mapper.AiMessageMapper;
import com.blog.mapper.AiUsageMapper;
import com.blog.mapper.AiUserModelMapper;
import com.blog.service.AiChatService;
import com.blog.service.AiKnowledgeService;
import com.blog.service.RedisService;
import com.blog.dto.response.AiKnowledgePayloadVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String KNOWLEDGE_META_START = "\n<!--AI_KNOWLEDGE_META_START-->";
    private static final String KNOWLEDGE_META_END = "<!--AI_KNOWLEDGE_META_END-->";

    private final AiConfigMapper configMapper;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiUsageMapper usageMapper;
    private final AiUserModelMapper userModelMapper;
    private final RedisService redisService;
    private final AiKnowledgeService aiKnowledgeService;
    private final ObjectMapper objectMapper;

    private static final String CACHE_AI_CONFIG = "blog:ai:config";
    private static final String CACHE_AI_CONFIGS = "blog:ai:configs";
    private static final String CACHE_AI_USER_MODEL = "blog:ai:user:model:";
    
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
        
        // 优先获取默认配置
        AiConfig config = configMapper.selectOne(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getEnabled, 1)
                .eq(AiConfig::getUseForChat, 1)
                .eq(AiConfig::getIsDefault, 1)
                .last("LIMIT 1"));
        if (config == null) {
            config = configMapper.selectOne(new LambdaQueryWrapper<AiConfig>()
                    .eq(AiConfig::getEnabled, 1)
                    .eq(AiConfig::getUseForChat, 1)
                    .orderByAsc(AiConfig::getSortOrder)
                    .last("LIMIT 1"));
        }
        if (config != null) {
            redisService.setWithMinutes(CACHE_AI_CONFIG, config, RedisService.EXPIRE_LONG);
        }
        return config;
    }

    @Override
    public AiConfig getStudyScoreConfig() {
        List<AiConfig> scoreConfigs = configMapper.selectList(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getEnabled, 1)
                .eq(AiConfig::getUseForStudyScore, 1)
                .orderByDesc(AiConfig::getIsDefaultStudyScore)
                .orderByDesc(AiConfig::getIsDefault)
                .orderByAsc(AiConfig::getSortOrder));
        if (!scoreConfigs.isEmpty()) {
            return scoreConfigs.get(0);
        }
        AiConfig fallback = getConfig();
        if (fallback != null && fallback.getEnabled() != null && fallback.getEnabled() == 1) {
            return fallback;
        }
        return null;
    }
    
    @Override
    public AiConfig getConfigById(Long configId) {
        if (configId == null) return getConfig();
        return configMapper.selectById(configId);
    }
    
    @Override
    public List<AiConfig> getAllConfigs() {
        return configMapper.selectList(new LambdaQueryWrapper<AiConfig>()
                .orderByAsc(AiConfig::getSortOrder)
                .orderByDesc(AiConfig::getIsDefault));
    }
    
    @Override
    public List<AiConfig> getEnabledConfigs() {
        return configMapper.selectList(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getEnabled, 1)
                .eq(AiConfig::getUseForChat, 1)
                .orderByAsc(AiConfig::getSortOrder)
                .orderByDesc(AiConfig::getIsDefault));
    }
    
    @Override
    public void addConfig(AiConfig config) {
        if (config.getSortOrder() == null) config.setSortOrder(0);
        if (config.getIsDefault() == null) config.setIsDefault(0);
        if (config.getUseForChat() == null) config.setUseForChat(1);
        if (config.getUseForStudyScore() == null) config.setUseForStudyScore(1);
        if (config.getIsDefaultStudyScore() == null) config.setIsDefaultStudyScore(0);
        if (config.getEnabled() == null) config.setEnabled(1);
        if (config.getUseForStudyScore() == 1 && countStudyScoreDefaultConfigs() == 0) {
            config.setIsDefaultStudyScore(1);
        }
        configMapper.insert(config);
        if (config.getIsDefault() != null && config.getIsDefault() == 1) {
            setDefaultConfig(config.getId());
        }
        if (config.getIsDefaultStudyScore() != null && config.getIsDefaultStudyScore() == 1) {
            setDefaultStudyScoreConfig(config.getId());
        }
        ensureDefaultConfigs();
        clearConfigCache();
    }

    @Override
    public void updateConfig(AiConfig config) {
        if (config.getId() == null) {
            addConfig(config);
        } else {
            configMapper.updateById(config);
            if (config.getIsDefault() != null && config.getIsDefault() == 1) {
                setDefaultConfig(config.getId());
            }
            if (config.getIsDefaultStudyScore() != null && config.getIsDefaultStudyScore() == 1) {
                setDefaultStudyScoreConfig(config.getId());
            }
            ensureDefaultConfigs();
        }
        clearConfigCache();
    }
    
    @Override
    public void deleteConfig(Long configId) {
        AiConfig config = configMapper.selectById(configId);
        if (config != null) {
            // 不允许删除唯一配置
            long count = configMapper.selectCount(null);
            if (count <= 1) {
                throw new RuntimeException("至少保留一个AI模型配置");
            }
            // 如果删除的是默认模型，将第一个设为默认
            if (config.getIsDefault() != null && config.getIsDefault() == 1) {
                AiConfig first = configMapper.selectOne(new LambdaQueryWrapper<AiConfig>()
                        .ne(AiConfig::getId, configId)
                        .orderByAsc(AiConfig::getSortOrder)
                        .last("LIMIT 1"));
                if (first != null) {
                    first.setIsDefault(1);
                    configMapper.updateById(first);
                }
            }
            if (config.getIsDefaultStudyScore() != null && config.getIsDefaultStudyScore() == 1) {
                AiConfig scoreFirst = configMapper.selectOne(new LambdaQueryWrapper<AiConfig>()
                        .ne(AiConfig::getId, configId)
                        .eq(AiConfig::getEnabled, 1)
                        .eq(AiConfig::getUseForStudyScore, 1)
                        .orderByAsc(AiConfig::getSortOrder)
                        .last("LIMIT 1"));
                if (scoreFirst != null) {
                    scoreFirst.setIsDefaultStudyScore(1);
                    configMapper.updateById(scoreFirst);
                }
            }
            configMapper.deleteById(configId);
            ensureDefaultConfigs();
            clearConfigCache();
        }
    }
    
    @Override
    public void setDefaultConfig(Long configId) {
        AiConfig target = configMapper.selectById(configId);
        if (target == null) {
            throw new RuntimeException("模型配置不存在");
        }
        if (target.getEnabled() == null || target.getEnabled() != 1) {
            throw new RuntimeException("请先启用该模型后再设置为默认");
        }
        if (target.getUseForChat() == null || target.getUseForChat() != 1) {
            throw new RuntimeException("该模型未启用AI对话场景");
        }
        // 先把所有配置的isDefault设为0
        List<AiConfig> all = configMapper.selectList(null);
        for (AiConfig c : all) {
            if (c.getIsDefault() != null && c.getIsDefault() == 1) {
                c.setIsDefault(0);
                configMapper.updateById(c);
            }
        }
        // 设置目标为默认
        target.setIsDefault(1);
        configMapper.updateById(target);
        clearConfigCache();
    }

    @Override
    public void setDefaultStudyScoreConfig(Long configId) {
        AiConfig target = configMapper.selectById(configId);
        if (target == null) {
            throw new RuntimeException("模型配置不存在");
        }
        if (target.getEnabled() == null || target.getEnabled() != 1) {
            throw new RuntimeException("请先启用该模型后再设置为评分默认");
        }
        if (target.getUseForStudyScore() == null || target.getUseForStudyScore() != 1) {
            throw new RuntimeException("该模型未启用学习模块AI评分");
        }
        List<AiConfig> scoreConfigs = configMapper.selectList(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getUseForStudyScore, 1));
        for (AiConfig config : scoreConfigs) {
            if (config.getIsDefaultStudyScore() != null && config.getIsDefaultStudyScore() == 1) {
                config.setIsDefaultStudyScore(0);
                configMapper.updateById(config);
            }
        }
        target.setIsDefaultStudyScore(1);
        configMapper.updateById(target);
        clearConfigCache();
    }
    
    @Override
    public AiConfig getUserModelConfig(Long userId) {
        // 先查缓存
        Object cached = redisService.get(CACHE_AI_USER_MODEL + userId);
        if (cached instanceof AiConfig) {
            return (AiConfig) cached;
        }
        
        // 查用户偏好
        AiUserModel userModel = userModelMapper.selectOne(new LambdaQueryWrapper<AiUserModel>()
                .eq(AiUserModel::getUserId, userId));
        
        AiConfig config = null;
        if (userModel != null) {
            config = configMapper.selectById(userModel.getConfigId());
            // 如果用户选择的模型已被禁用或删除，回退到默认
            if (config == null || config.getEnabled() != 1) {
                config = null;
            }
            if (config != null && (config.getUseForChat() == null || config.getUseForChat() != 1)) {
                config = null;
            }
        }
        
        // 回退到默认配置
        if (config == null) {
            config = getConfig();
        }
        
        if (config != null) {
            redisService.setWithMinutes(CACHE_AI_USER_MODEL + userId, config, RedisService.EXPIRE_LONG);
        }
        return config;
    }
    
    @Override
    public void switchUserModel(Long userId, Long configId) {
        // 验证配置存在且已启用
        AiConfig config = configMapper.selectById(configId);
        if (config == null || config.getEnabled() != 1 || config.getUseForChat() == null || config.getUseForChat() != 1) {
            throw new RuntimeException("该模型不可用");
        }
        
        AiUserModel userModel = userModelMapper.selectOne(new LambdaQueryWrapper<AiUserModel>()
                .eq(AiUserModel::getUserId, userId));
        
        if (userModel == null) {
            userModel = new AiUserModel();
            userModel.setUserId(userId);
            userModel.setConfigId(configId);
            userModelMapper.insert(userModel);
        } else {
            userModel.setConfigId(configId);
            userModelMapper.updateById(userModel);
        }
        
        // 清除用户模型缓存
        redisService.delete(CACHE_AI_USER_MODEL + userId);
    }
    
    private void clearConfigCache() {
        redisService.delete(CACHE_AI_CONFIG);
        redisService.delete(CACHE_AI_CONFIGS);
    }

    private long countStudyScoreDefaultConfigs() {
        return configMapper.selectCount(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getUseForStudyScore, 1)
                .eq(AiConfig::getIsDefaultStudyScore, 1));
    }

    private void ensureDefaultConfigs() {
        List<AiConfig> allConfigs = configMapper.selectList(null);
        for (AiConfig config : allConfigs) {
            boolean changed = false;
            if (config.getIsDefault() != null && config.getIsDefault() == 1
                    && (config.getEnabled() == null || config.getEnabled() != 1
                    || config.getUseForChat() == null || config.getUseForChat() != 1)) {
                config.setIsDefault(0);
                changed = true;
            }
            if (config.getIsDefaultStudyScore() != null && config.getIsDefaultStudyScore() == 1
                    && (config.getEnabled() == null || config.getEnabled() != 1
                    || config.getUseForStudyScore() == null || config.getUseForStudyScore() != 1)) {
                config.setIsDefaultStudyScore(0);
                changed = true;
            }
            if (changed) {
                configMapper.updateById(config);
            }
        }

        List<AiConfig> chatConfigs = configMapper.selectList(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getEnabled, 1)
                .eq(AiConfig::getUseForChat, 1)
                .orderByAsc(AiConfig::getSortOrder)
                .orderByAsc(AiConfig::getId));
        boolean hasChatDefault = chatConfigs.stream().anyMatch(item -> item.getIsDefault() != null && item.getIsDefault() == 1);
        if (!hasChatDefault && !chatConfigs.isEmpty()) {
            AiConfig first = chatConfigs.get(0);
            first.setIsDefault(1);
            configMapper.updateById(first);
        }

        List<AiConfig> scoreConfigs = configMapper.selectList(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getEnabled, 1)
                .eq(AiConfig::getUseForStudyScore, 1)
                .orderByAsc(AiConfig::getSortOrder)
                .orderByAsc(AiConfig::getId));
        boolean hasScoreDefault = scoreConfigs.stream().anyMatch(item -> item.getIsDefaultStudyScore() != null && item.getIsDefaultStudyScore() == 1);
        if (!hasScoreDefault && !scoreConfigs.isEmpty()) {
            AiConfig first = scoreConfigs.get(0);
            first.setIsDefaultStudyScore(1);
            configMapper.updateById(first);
        }
    }

    @Override
    public boolean checkDailyLimit(Long userId) {
        AiConfig config = getUserModelConfig(userId);
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
        // 记录当前用户选择的模型配置ID
        AiConfig userConfig = getUserModelConfig(userId);
        if (userConfig != null) {
            conversation.setConfigId(userConfig.getId());
        }
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
        // 验证会话
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            callback.onError("会话不存在");
            return;
        }
        
        // 始终使用用户当前选择的模型（支持会话中途切换模型）
        AiConfig config = getUserModelConfig(userId);
        
        // 如果用户当前模型与会话记录不同，更新会话的configId
        if (config != null && config.getId() != null 
                && !config.getId().equals(conversation.getConfigId())) {
            conversation.setConfigId(config.getId());
            conversationMapper.updateById(conversation);
        }
        
        if (config == null || config.getEnabled() != 1 || config.getUseForChat() == null || config.getUseForChat() != 1) {
            callback.onError("AI功能未启用");
            return;
        }
        
        if (!checkDailyLimit(userId)) {
            callback.onError("今日使用次数已达上限");
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
        AiKnowledgePayloadVO knowledgePayload = aiKnowledgeService.buildKnowledgeContext(userId, message);
        String knowledgeContext = knowledgePayload != null ? knowledgePayload.getPromptContext() : null;
        String knowledgeJson = buildKnowledgeJson(knowledgePayload);
        
        // 调用AI API
        callAiApi(config, history, knowledgeContext, knowledgeJson, callback, conversationId, userId);
    }
    
    private void callAiApi(AiConfig config, List<AiMessage> history, String knowledgeContext, String knowledgeJson, AiStreamCallback callback,
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
            if (StringUtils.hasText(knowledgeContext)) {
                Map<String, String> knowledgeMsg = new HashMap<>();
                knowledgeMsg.put("role", "system");
                knowledgeMsg.put("content", knowledgeContext);
                messages.add(knowledgeMsg);
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
                baseUrl = "https://api.deepseek.com/v1";
            }
            // 去除尾部斜杠
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            
            String fullUrl = baseUrl + "/chat/completions";
            log.info("调用AI API: {}, model: {}", fullUrl, config.getModel());
            
            Request request = new Request.Builder()
                    .url(fullUrl)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();
            
            callback.onStart();
            if (StringUtils.hasText(knowledgeJson)) {
                callback.onKnowledge(knowledgeJson);
            }
            
            StringBuilder fullResponse = new StringBuilder();
            StringBuilder fullThinking = new StringBuilder();
            AtomicBoolean streamFinished = new AtomicBoolean(false);
            // 判断该模型是否支持深度思考
            boolean thinkingEnabled = config.getSupportThinking() != null && config.getSupportThinking() == 1;
            
            EventSource.Factory factory = EventSources.createFactory(httpClient);
            factory.newEventSource(request, new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if (streamFinished.get()) {
                        return;
                    }
                    if (handleCancelledStream(eventSource, streamFinished, callback, conversationId, userId,
                            fullResponse, fullThinking, knowledgeJson)) {
                        return;
                    }
                    if ("[DONE]".equals(data)) {
                        if (!streamFinished.compareAndSet(false, true)) {
                            return;
                        }
                        // 保存AI回复（包含思考内容）
                        saveAiResponse(conversationId, userId, fullResponse.toString(), 
                                fullThinking.length() > 0 ? fullThinking.toString() : null,
                                knowledgeJson);
                        callback.onComplete(fullResponse.toString());
                        eventSource.cancel();
                        return;
                    }
                    
                    try {
                        JsonNode node = objectMapper.readTree(data);
                        JsonNode choices = node.get("choices");
                        if (choices != null && choices.isArray() && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null) {
                                // 解析深度思考内容（reasoning_content）
                                if (thinkingEnabled && delta.has("reasoning_content") 
                                        && !delta.get("reasoning_content").isNull()) {
                                    String reasoning = delta.get("reasoning_content").asText();
                                    if (reasoning != null && !reasoning.isEmpty()) {
                                        fullThinking.append(reasoning);
                                        callback.onThinking(reasoning);
                                    }
                                }
                                // 解析正常回复内容
                                if (delta.has("content") && !delta.get("content").isNull()) {
                                    String content = delta.get("content").asText();
                                    if (content != null && !content.isEmpty()) {
                                        fullResponse.append(content);
                                        callback.onToken(content);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析AI响应失败: {}", e.getMessage());
                    }
                }
                
                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    if (!streamFinished.compareAndSet(false, true)) {
                        return;
                    }
                    if (callback.isCancelled()) {
                        savePartialAiResponse(conversationId, userId, fullResponse, fullThinking, knowledgeJson);
                        eventSource.cancel();
                        return;
                    }
                    String error = "AI服务请求失败";
                    if (response != null) {
                        int code = response.code();
                        String body = "";
                        try {
                            body = response.body() != null ? response.body().string() : "";
                        } catch (Exception e) {
                            body = "读取错误响应失败";
                        }
                        error = "HTTP " + code + ": " + (body.isEmpty() ? response.message() : body);
                        log.error("AI API调用失败, HTTP {}, body: {}", code, body);
                    } else if (t != null) {
                        error = t.getClass().getSimpleName() + ": " + (t.getMessage() != null ? t.getMessage() : "连接失败");
                        log.error("AI API调用异常", t);
                    }
                    callback.onError(error);
                    eventSource.cancel();
                }
            });
            
        } catch (Exception e) {
            log.error("调用AI API异常: {}", e.getMessage());
            callback.onError("AI服务异常: " + e.getMessage());
        }
    }

    private boolean handleCancelledStream(EventSource eventSource,
                                          AtomicBoolean streamFinished,
                                          AiStreamCallback callback,
                                          Long conversationId,
                                          Long userId,
                                          StringBuilder fullResponse,
                                          StringBuilder fullThinking,
                                          String knowledgeJson) {
        if (!callback.isCancelled()) {
            return false;
        }
        if (!streamFinished.compareAndSet(false, true)) {
            return true;
        }
        savePartialAiResponse(conversationId, userId, fullResponse, fullThinking, knowledgeJson);
        log.info("检测到客户端已取消AI生成，终止上游流式请求，conversationId={}", conversationId);
        eventSource.cancel();
        return true;
    }

    private void savePartialAiResponse(Long conversationId,
                                       Long userId,
                                       StringBuilder fullResponse,
                                       StringBuilder fullThinking,
                                       String knowledgeJson) {
        String content = fullResponse == null ? "" : fullResponse.toString();
        String thinkingContent = fullThinking == null || fullThinking.length() == 0 ? null : fullThinking.toString();
        if (!StringUtils.hasText(content) && !StringUtils.hasText(thinkingContent)) {
            return;
        }
        saveAiResponse(conversationId, userId, content, thinkingContent, knowledgeJson);
    }
    
    private void saveAiResponse(Long conversationId, Long userId, String content, String thinkingContent, String knowledgeJson) {
        AiMessage aiMessage = new AiMessage();
        aiMessage.setConversationId(conversationId);
        aiMessage.setUserId(userId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(content);
        aiMessage.setThinkingContent(packThinkingContent(thinkingContent, knowledgeJson));
        aiMessage.setTokens(content.length() / 4); // 粗略估算token数
        messageMapper.insert(aiMessage);
        
        // 更新使用统计
        usageMapper.incrementUsage(userId, LocalDate.now(), aiMessage.getTokens());
    }

    private String buildKnowledgeJson(AiKnowledgePayloadVO knowledgePayload) {
        if (knowledgePayload == null || knowledgePayload.getSources() == null || knowledgePayload.getSources().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(knowledgePayload.getSources());
        } catch (Exception e) {
            log.warn("序列化AI知识来源失败: {}", e.getMessage());
            return null;
        }
    }

    private String packThinkingContent(String thinkingContent, String knowledgeJson) {
        if (!StringUtils.hasText(knowledgeJson)) {
            return thinkingContent;
        }
        String safeThinkingContent = thinkingContent == null ? "" : thinkingContent;
        return safeThinkingContent + KNOWLEDGE_META_START + knowledgeJson + KNOWLEDGE_META_END;
    }
}
