package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog.search.elasticsearch")
public class ElasticsearchProperties {

    /**
     * 是否启用 Elasticsearch 搜索
     */
    private boolean enabled = false;

    /**
     * Elasticsearch 服务地址
     */
    private String endpoint = "http://localhost:9200";

    /**
     * 用户名，可为空
     */
    private String username;

    /**
     * 密码，可为空
     */
    private String password;

    /**
     * 文章索引名
     */
    private String articleIndex = "blog_article_search";

    /**
     * 面试题索引名
     */
    private String studyQuestionIndex = "blog_study_question_search";

    /**
     * 用户索引名
     */
    private String userIndex = "blog_user_search";

    /**
     * 中文分词器，建议安装 IK 插件
     */
    private String analyzer = "ik_max_word";

    /**
     * 搜索分词器
     */
    private String searchAnalyzer = "ik_smart";

    /**
     * 连接超时
     */
    private int connectTimeoutMs = 3000;

    /**
     * 读取超时
     */
    private int readTimeoutMs = 5000;
}
