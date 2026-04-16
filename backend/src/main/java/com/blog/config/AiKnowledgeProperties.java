package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog.ai.knowledge")
public class AiKnowledgeProperties {

    /**
     * 是否启用项目知识增强问答
     */
    private boolean enabled = true;

    /**
     * 单次注入的相关文章条数
     */
    private int articleLimit = 3;

    /**
     * 单次注入的面试题条数
     */
    private int studyQuestionLimit = 3;

    /**
     * 单篇文章注入给大模型的最大正文长度
     */
    private int articleContentMaxLength = 800;

    /**
     * 单道面试题注入给大模型的最大题干长度
     */
    private int questionStemMaxLength = 400;

    /**
     * 单道面试题注入给大模型的最大答案长度
     */
    private int questionAnswerMaxLength = 1000;
}
