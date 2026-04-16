package com.blog.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiKnowledgePayloadVO {

    /**
     * 注入给大模型的项目知识上下文
     */
    private String promptContext;

    /**
     * 供前端展示的结构化来源卡片
     */
    private List<AiKnowledgeSourceVO> sources = new ArrayList<>();
}
