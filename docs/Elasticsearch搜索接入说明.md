# Elasticsearch 搜索接入说明

## 功能范围

当前项目已经接入统一搜索能力，支持：

- 博客文章搜索
- 面试题/学习题库搜索
- 用户搜索
- Elasticsearch 不可用时自动回退到数据库 `like` 搜索

接口：

- `GET /api/search`
- `GET /api/admin/search/status`
- `POST /api/admin/search/rebuild?scope=all`

## 推荐版本

建议使用 Elasticsearch `8.x`，并安装 IK 中文分词插件。

如果没有 IK 分词器，索引创建会失败，系统会自动回退到数据库搜索。

## Docker 示例

```bash
docker run -d \
  --name blog-es \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:8.12.2
```

## 配置项

在 `application-dev.yml` 或 `application-prod.yml` 中增加：

```yml
blog:
  search:
    elasticsearch:
      enabled: true
      endpoint: http://localhost:9200
      username:
      password:
      article-index: blog_article_search
      study-question-index: blog_study_question_search
      analyzer: ik_max_word
      search-analyzer: ik_smart
      connect-timeout-ms: 3000
      read-timeout-ms: 5000
```

## 初始化索引

配置完成后调用：

```http
POST /api/admin/search/rebuild?scope=all
```

可选 `scope`：

- `all`
- `article`
- `study`

## 同步策略

当前代码已覆盖：

- 文章新增、编辑、删除、发布状态变更
- 面试题新增、编辑、删除

高频计数字段如浏览量、点赞数允许短暂延迟；如果后面要做更精准的热度排序，建议再补一个定时刷新任务。
