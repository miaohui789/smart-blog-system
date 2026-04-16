package com.blog.service;

import com.blog.dto.response.AiKnowledgePayloadVO;

public interface AiKnowledgeService {

    /**
     * 构建项目知识增强上下文
     */
    AiKnowledgePayloadVO buildKnowledgeContext(Long userId, String question);
}
