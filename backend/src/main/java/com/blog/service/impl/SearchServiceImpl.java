package com.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.constant.StudyConstants;
import com.blog.common.result.PageResult;
import com.blog.config.ElasticsearchProperties;
import com.blog.dto.response.HotSearchBoardVO;
import com.blog.dto.response.HotSearchItemVO;
import com.blog.dto.response.SearchAllVO;
import com.blog.dto.response.SearchArticleVO;
import com.blog.dto.response.SearchStudyQuestionVO;
import com.blog.dto.response.TagVO;
import com.blog.dto.response.UserVO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Category;
import com.blog.entity.StudyCategory;
import com.blog.entity.StudyQuestion;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.StudyQuestionMapper;
import com.blog.service.ArticleService;
import com.blog.service.ArticleTagService;
import com.blog.service.CategoryService;
import com.blog.service.SearchService;
import com.blog.service.StudyCategoryService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import com.blog.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.ZSetOperations;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String ENGINE_ELASTICSEARCH = "elasticsearch";
    private static final String ENGINE_DATABASE = "database";
    private static final ZoneId HOT_SEARCH_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter HOT_SEARCH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOT_SEARCH_KEY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final List<DateTimeFormatter> SUPPORTED_DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    );

    private final ArticleService articleService;
    private final ArticleTagService articleTagService;
    private final TagService tagService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StudyQuestionMapper studyQuestionMapper;
    private final StudyCategoryService studyCategoryService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final ElasticsearchProperties elasticsearchProperties;
    private final OkHttpClient okHttpClient;

    private volatile boolean articleIndexReady;
    private volatile boolean studyQuestionIndexReady;
    private volatile boolean userIndexReady;

    public SearchServiceImpl(ArticleService articleService,
                             ArticleTagService articleTagService,
                             TagService tagService,
                             UserService userService,
                             CategoryService categoryService,
                             StudyQuestionMapper studyQuestionMapper,
                             StudyCategoryService studyCategoryService,
                             RedisService redisService,
                             ObjectMapper objectMapper,
                             ElasticsearchProperties elasticsearchProperties) {
        this.articleService = articleService;
        this.articleTagService = articleTagService;
        this.tagService = tagService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.studyQuestionMapper = studyQuestionMapper;
        this.studyCategoryService = studyCategoryService;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.elasticsearchProperties = elasticsearchProperties;
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(elasticsearchProperties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(elasticsearchProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(elasticsearchProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public SearchAllVO search(String keyword, Integer page, Integer pageSize) {
        return search(keyword, page, pageSize, false);
    }

    @Override
    public SearchAllVO search(String keyword, Integer page, Integer pageSize, boolean recordHot) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        if (!StringUtils.hasText(normalizedKeyword)) {
            return emptyResult(ENGINE_DATABASE, currentPage, currentPageSize);
        }
        if (recordHot) {
            recordHotKeyword(normalizedKeyword);
        }
        if (isElasticsearchEnabled()) {
            try {
                ensureIndices();
                return searchByElasticsearch(normalizedKeyword, currentPage, currentPageSize);
            } catch (Exception e) {
                log.warn("Elasticsearch 搜索失败，已回退到数据库搜索: {}", e.getMessage());
            }
        }
        return searchByDatabase(normalizedKeyword, currentPage, currentPageSize);
    }

    @Override
    public HotSearchBoardVO getHotSearchBoard(Integer limit) {
        int currentLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 20);
        ZonedDateTime now = ZonedDateTime.now(HOT_SEARCH_ZONE);
        String cacheKey = buildHotSearchKey(now.toLocalDate());
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisService.zReverseRangeWithScores(cacheKey, 0, currentLimit - 1);

        List<HotSearchItemVO> list = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple == null || tuple.getValue() == null) {
                continue;
            }
            HotSearchItemVO item = new HotSearchItemVO();
            item.setRank(rank++);
            item.setKeyword(String.valueOf(tuple.getValue()));
            item.setScore(tuple.getScore() == null ? 0L : tuple.getScore().longValue());
            list.add(item);
        }

        HotSearchBoardVO board = new HotSearchBoardVO();
        board.setDate(now.format(HOT_SEARCH_DATE_FORMATTER));
        board.setTimezone("北京时间");
        board.setPeriod("00:00 - 24:00");
        board.setExpiresInSeconds(getSecondsUntilMidnight(now));
        board.setList(list);
        return board;
    }

    @Override
    public void syncArticle(Long articleId) {
        if (!isElasticsearchEnabled() || articleId == null) {
            return;
        }
        try {
            ensureArticleIndex();
            Article article = articleService.getById(articleId);
            if (!isSearchableArticle(article)) {
                deleteArticle(articleId);
                return;
            }
            upsertDocument(elasticsearchProperties.getArticleIndex(), String.valueOf(articleId), buildArticleDocument(article));
        } catch (Exception e) {
            log.warn("同步文章搜索索引失败, articleId={}, reason={}", articleId, e.getMessage());
        }
    }

    @Override
    public void deleteArticle(Long articleId) {
        if (!isElasticsearchEnabled() || articleId == null) {
            return;
        }
        try {
            deleteDocument(elasticsearchProperties.getArticleIndex(), String.valueOf(articleId));
        } catch (Exception e) {
            log.warn("删除文章搜索索引失败, articleId={}, reason={}", articleId, e.getMessage());
        }
    }

    @Override
    public void syncUser(Long userId) {
        if (!isElasticsearchEnabled() || userId == null) {
            return;
        }
        try {
            ensureUserIndex();
            User user = userService.getById(userId);
            if (!isSearchableUser(user)) {
                deleteUser(userId);
                return;
            }
            upsertDocument(elasticsearchProperties.getUserIndex(), String.valueOf(userId), buildUserDocument(user));
        } catch (Exception e) {
            log.warn("同步用户搜索索引失败, userId={}, reason={}", userId, e.getMessage());
        }
    }

    @Override
    public void deleteUser(Long userId) {
        if (!isElasticsearchEnabled() || userId == null) {
            return;
        }
        try {
            deleteDocument(elasticsearchProperties.getUserIndex(), String.valueOf(userId));
        } catch (Exception e) {
            log.warn("删除用户搜索索引失败, userId={}, reason={}", userId, e.getMessage());
        }
    }

    @Override
    public void syncArticlesByUserId(Long userId) {
        if (!isElasticsearchEnabled() || userId == null) {
            return;
        }
        List<Article> articles = articleService.list(new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId));
        articles.forEach(article -> syncArticle(article.getId()));
    }

    @Override
    public void syncArticlesByCategoryId(Long categoryId) {
        if (!isElasticsearchEnabled() || categoryId == null) {
            return;
        }
        List<Article> articles = articleService.list(new LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, categoryId));
        articles.forEach(article -> syncArticle(article.getId()));
    }

    @Override
    public void syncArticlesByTagId(Long tagId) {
        if (!isElasticsearchEnabled() || tagId == null) {
            return;
        }
        List<Long> articleIds = articleTagService.list(new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getTagId, tagId))
                .stream()
                .map(ArticleTag::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        articleIds.forEach(this::syncArticle);
    }

    @Override
    public void syncStudyQuestion(Long questionId) {
        if (!isElasticsearchEnabled() || questionId == null) {
            return;
        }
        try {
            ensureStudyQuestionIndex();
            StudyQuestion question = studyQuestionMapper.selectById(questionId);
            if (!isSearchableStudyQuestion(question)) {
                deleteStudyQuestion(questionId);
                return;
            }
            upsertDocument(elasticsearchProperties.getStudyQuestionIndex(), String.valueOf(questionId), buildStudyQuestionDocument(question));
        } catch (Exception e) {
            log.warn("同步面试题搜索索引失败, questionId={}, reason={}", questionId, e.getMessage());
        }
    }

    @Override
    public void deleteStudyQuestion(Long questionId) {
        if (!isElasticsearchEnabled() || questionId == null) {
            return;
        }
        try {
            deleteDocument(elasticsearchProperties.getStudyQuestionIndex(), String.valueOf(questionId));
        } catch (Exception e) {
            log.warn("删除面试题搜索索引失败, questionId={}, reason={}", questionId, e.getMessage());
        }
    }

    @Override
    public void syncStudyQuestionsByCategoryId(Long categoryId) {
        if (!isElasticsearchEnabled() || categoryId == null) {
            return;
        }
        List<StudyQuestion> questions = studyQuestionMapper.selectList(new LambdaQueryWrapper<StudyQuestion>()
                .eq(StudyQuestion::getCategoryId, categoryId));
        questions.forEach(question -> syncStudyQuestion(question.getId()));
    }

    @Override
    public Map<String, Object> rebuildIndex(String scope) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", elasticsearchProperties.isEnabled());
        result.put("scope", scope);
        if (!isElasticsearchEnabled()) {
            result.put("message", "Elasticsearch 未启用，当前仅支持数据库兜底搜索");
            return result;
        }
        try {
            ensureIndices();
            String normalizedScope = normalizeScope(scope);
            int articleCount = 0;
            int studyQuestionCount = 0;
            int userCount = 0;
            if ("all".equals(normalizedScope) || "article".equals(normalizedScope)) {
                List<Article> articles = articleService.list(new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1));
                for (Article article : articles) {
                    syncArticle(article.getId());
                    articleCount++;
                }
            }
            if ("all".equals(normalizedScope) || "study".equals(normalizedScope)) {
                List<StudyQuestion> questions = studyQuestionMapper.selectList(new LambdaQueryWrapper<StudyQuestion>()
                        .eq(StudyQuestion::getStatus, StudyConstants.STATUS_ENABLED)
                        .eq(StudyQuestion::getReviewStatus, StudyConstants.REVIEW_PUBLISHED));
                for (StudyQuestion question : questions) {
                    syncStudyQuestion(question.getId());
                    studyQuestionCount++;
                }
            }
            if ("all".equals(normalizedScope) || "user".equals(normalizedScope)) {
                List<User> users = userService.list();
                for (User user : users) {
                    syncUser(user.getId());
                    userCount++;
                }
            }
            result.put("success", true);
            result.put("articleIndexed", articleCount);
            result.put("studyQuestionIndexed", studyQuestionCount);
            result.put("userIndexed", userCount);
            result.put("message", "重建索引完成");
            return result;
        } catch (Exception e) {
            log.error("重建搜索索引失败", e);
            result.put("success", false);
            result.put("message", "重建索引失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> getSearchStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", elasticsearchProperties.isEnabled());
        result.put("endpoint", elasticsearchProperties.getEndpoint());
        result.put("articleIndex", elasticsearchProperties.getArticleIndex());
        result.put("studyQuestionIndex", elasticsearchProperties.getStudyQuestionIndex());
        result.put("userIndex", elasticsearchProperties.getUserIndex());
        result.put("analyzer", elasticsearchProperties.getAnalyzer());
        result.put("searchAnalyzer", elasticsearchProperties.getSearchAnalyzer());
        if (!isElasticsearchEnabled()) {
            result.put("available", false);
            result.put("engine", ENGINE_DATABASE);
            result.put("message", "Elasticsearch 未启用，当前使用数据库搜索");
            return result;
        }
        try {
            ensureIndices();
            result.put("available", true);
            result.put("engine", ENGINE_ELASTICSEARCH);
            result.put("message", "Elasticsearch 可用");
        } catch (Exception e) {
            result.put("available", false);
            result.put("engine", ENGINE_DATABASE);
            result.put("message", "Elasticsearch 不可用，原因: " + e.getMessage());
        }
        return result;
    }

    private SearchAllVO searchByElasticsearch(String keyword, int page, int pageSize) throws IOException {
        SearchAllVO result = new SearchAllVO();
        result.setEngine(ENGINE_ELASTICSEARCH);
        result.setArticles(searchArticlesByElasticsearch(keyword, page, pageSize));
        result.setStudyQuestions(searchStudyQuestionsByElasticsearch(keyword, page, pageSize));
        result.setUsers(searchUsersByElasticsearch(keyword, 5));
        return result;
    }

    private SearchAllVO searchByDatabase(String keyword, int page, int pageSize) {
        SearchAllVO result = new SearchAllVO();
        result.setEngine(ENGINE_DATABASE);
        result.setArticles(searchArticlesByDatabase(keyword, page, pageSize));
        result.setStudyQuestions(searchStudyQuestionsByDatabase(keyword, page, pageSize));
        result.setUsers(searchUsers(keyword, 5));
        return result;
    }

    private SearchAllVO emptyResult(String engine, int page, int pageSize) {
        SearchAllVO result = new SearchAllVO();
        result.setEngine(engine);
        result.setArticles(PageResult.of(Collections.emptyList(), 0L, page, pageSize));
        result.setStudyQuestions(PageResult.of(Collections.emptyList(), 0L, page, pageSize));
        result.setUsers(Collections.emptyList());
        return result;
    }

    private PageResult<SearchArticleVO> searchArticlesByElasticsearch(String keyword, int page, int pageSize) throws IOException {
        int from = (page - 1) * pageSize;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", from);
        body.put("size", pageSize);
        body.put("_source", List.of(
                "id", "categoryId", "categoryName", "title", "summary", "cover",
                "viewCount", "likeCount", "commentCount", "favoriteCount", "publishTime",
                "author", "tags"
        ));
        body.put("query", buildArticleSearchQuery(keyword));
        body.put("highlight", buildHighlight(List.of("title", "summary")));
        body.put("sort", List.of(
                Map.of("_score", Map.of("order", "desc")),
                Map.of("publishTime", Map.of("order", "desc"))
        ));

        JsonNode root = executeJsonRequest("POST", "/" + elasticsearchProperties.getArticleIndex() + "/_search", body);
        JsonNode hitsNode = root.path("hits");
        long total = hitsNode.path("total").path("value").asLong(0L);
        List<SearchArticleVO> list = new ArrayList<>();
        for (JsonNode hitNode : hitsNode.path("hits")) {
            JsonNode source = hitNode.path("_source");
            SearchArticleVO item = buildArticleSearchVOFromSource(source);
            JsonNode highlight = hitNode.path("highlight");
            item.setTitleHighlight(readHighlight(highlight, "title"));
            item.setSummaryHighlight(readHighlight(highlight, "summary"));
            list.add(item);
        }
        return PageResult.of(list, total, page, pageSize);
    }

    private PageResult<SearchStudyQuestionVO> searchStudyQuestionsByElasticsearch(String keyword, int page, int pageSize) throws IOException {
        int from = (page - 1) * pageSize;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", from);
        body.put("size", pageSize);
        body.put("_source", List.of(
                "id", "categoryId", "categoryName", "questionNo", "questionCode", "title",
                "answerSummary", "keywords", "difficulty", "viewCount", "studyCount", "publishTime"
        ));
        body.put("query", buildStudyQuestionSearchQuery(keyword));
        body.put("highlight", buildHighlight(List.of("title", "answerSummary")));
        body.put("sort", List.of(
                Map.of("_score", Map.of("order", "desc")),
                Map.of("publishTime", Map.of("order", "desc")),
                Map.of("updateTime", Map.of("order", "desc"))
        ));

        JsonNode root = executeJsonRequest("POST", "/" + elasticsearchProperties.getStudyQuestionIndex() + "/_search", body);
        JsonNode hitsNode = root.path("hits");
        long total = hitsNode.path("total").path("value").asLong(0L);
        List<SearchStudyQuestionVO> list = new ArrayList<>();
        for (JsonNode hitNode : hitsNode.path("hits")) {
            JsonNode source = hitNode.path("_source");
            SearchStudyQuestionVO item = buildStudyQuestionSearchVOFromSource(source);
            JsonNode highlight = hitNode.path("highlight");
            item.setTitleHighlight(readHighlight(highlight, "title"));
            item.setAnswerSummaryHighlight(readHighlight(highlight, "answerSummary"));
            list.add(item);
        }
        return PageResult.of(list, total, page, pageSize);
    }

    private PageResult<SearchArticleVO> searchArticlesByDatabase(String keyword, int page, int pageSize) {
        List<Tag> matchedTags = tagService.list(new LambdaQueryWrapper<Tag>()
                .like(Tag::getName, keyword));
        List<Long> tagArticleIds = Collections.emptyList();
        if (!matchedTags.isEmpty()) {
            List<Long> tagIds = matchedTags.stream().map(Tag::getId).collect(Collectors.toList());
            tagArticleIds = articleTagService.list(new LambdaQueryWrapper<ArticleTag>()
                            .in(ArticleTag::getTagId, tagIds))
                    .stream()
                    .map(ArticleTag::getArticleId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        List<Long> finalTagArticleIds = tagArticleIds;
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .and(w -> {
                    w.like(Article::getTitle, keyword)
                            .or()
                            .like(Article::getSummary, keyword)
                            .or()
                            .like(Article::getContent, keyword);
                    if (!finalTagArticleIds.isEmpty()) {
                        w.or().in(Article::getId, finalTagArticleIds);
                    }
                })
                .orderByDesc(Article::getPublishTime);
        Page<Article> pageResult = articleService.page(new Page<>(page, pageSize), wrapper);
        List<SearchArticleVO> list = pageResult.getRecords().stream()
                .map(this::buildArticleSearchVO)
                .collect(Collectors.toList());
        return PageResult.of(list, pageResult.getTotal(), page, pageSize);
    }

    private PageResult<SearchStudyQuestionVO> searchStudyQuestionsByDatabase(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyQuestion::getStatus, StudyConstants.STATUS_ENABLED)
                .eq(StudyQuestion::getReviewStatus, StudyConstants.REVIEW_PUBLISHED)
                .and(w -> w.like(StudyQuestion::getTitle, keyword)
                        .or().like(StudyQuestion::getKeywords, keyword)
                        .or().like(StudyQuestion::getAnswerSummary, keyword)
                        .or().like(StudyQuestion::getQuestionStem, keyword)
                        .or().like(StudyQuestion::getStandardAnswer, keyword))
                .orderByDesc(StudyQuestion::getUpdateTime)
                .orderByDesc(StudyQuestion::getId);
        Page<StudyQuestion> pageResult = new Page<>(page, pageSize);
        Page<StudyQuestion> resultPage = studyQuestionMapper.selectPage(pageResult, wrapper);
        List<SearchStudyQuestionVO> list = resultPage.getRecords().stream()
                .map(this::buildStudyQuestionSearchVO)
                .collect(Collectors.toList());
        return PageResult.of(list, resultPage.getTotal(), page, pageSize);
    }

    private List<UserVO> searchUsers(String keyword, int limit) {
        return userService.searchUsers(keyword, limit).stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private List<UserVO> searchUsersByElasticsearch(String keyword, int limit) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", limit);
        body.put("_source", List.of(
                "id", "username", "nickname", "avatar", "intro",
                "website", "status", "userLevel", "vipLevel", "createTime"
        ));
        body.put("query", buildUserSearchQuery(keyword));
        body.put("sort", List.of(
                Map.of("_score", Map.of("order", "desc")),
                Map.of("createTime", Map.of("order", "desc"))
        ));

        JsonNode root = executeJsonRequest("POST", "/" + elasticsearchProperties.getUserIndex() + "/_search", body);
        List<UserVO> users = new ArrayList<>();
        for (JsonNode hitNode : root.path("hits").path("hits")) {
            users.add(buildUserSearchVOFromSource(hitNode.path("_source")));
        }
        return users;
    }

    private SearchArticleVO buildArticleSearchVO(Article article) {
        SearchArticleVO vo = new SearchArticleVO();
        vo.setId(article.getId());
        vo.setCategoryId(article.getCategoryId());
        vo.setTitle(article.getTitle());
        vo.setSummary(firstNonBlank(article.getSummary(), shortenText(stripText(article.getContent()), 140)));
        vo.setCover(article.getCover());
        vo.setViewCount(defaultInt(article.getViewCount()));
        vo.setLikeCount(defaultInt(article.getLikeCount()));
        vo.setCommentCount(defaultInt(article.getCommentCount()));
        vo.setFavoriteCount(defaultInt(article.getFavoriteCount()));
        vo.setPublishTime(article.getPublishTime());

        if (article.getCategoryId() != null) {
            Category category = categoryService.getById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        User author = userService.getById(article.getUserId());
        if (author != null) {
            UserVO userVO = new UserVO();
            userVO.setId(author.getId());
            userVO.setUsername(author.getUsername());
            userVO.setNickname(author.getNickname());
            userVO.setAvatar(author.getAvatar());
            userVO.setUserLevel(author.getUserLevel());
            userVO.setVipLevel(author.getVipLevel());
            vo.setAuthor(userVO);
        }
        vo.setTags(loadArticleTags(article.getId()));
        return vo;
    }

    private SearchArticleVO buildArticleSearchVOFromSource(JsonNode source) {
        SearchArticleVO vo = new SearchArticleVO();
        vo.setId(readLong(source, "id"));
        vo.setCategoryId(readLong(source, "categoryId"));
        vo.setCategoryName(readText(source, "categoryName"));
        vo.setTitle(readText(source, "title"));
        vo.setSummary(readText(source, "summary"));
        vo.setCover(readText(source, "cover"));
        vo.setViewCount(readInteger(source, "viewCount"));
        vo.setLikeCount(readInteger(source, "likeCount"));
        vo.setCommentCount(readInteger(source, "commentCount"));
        vo.setFavoriteCount(readInteger(source, "favoriteCount"));
        vo.setPublishTime(parseDateTime(readText(source, "publishTime")));

        JsonNode authorNode = source.path("author");
        if (!authorNode.isMissingNode() && !authorNode.isNull()) {
            vo.setAuthor(objectMapper.convertValue(authorNode, UserVO.class));
        }

        JsonNode tagsNode = source.path("tags");
        if (tagsNode.isArray()) {
            List<TagVO> tags = new ArrayList<>();
            for (JsonNode tagNode : tagsNode) {
                tags.add(objectMapper.convertValue(tagNode, TagVO.class));
            }
            vo.setTags(tags);
        }
        return vo;
    }

    private SearchStudyQuestionVO buildStudyQuestionSearchVO(StudyQuestion question) {
        SearchStudyQuestionVO vo = new SearchStudyQuestionVO();
        vo.setId(question.getId());
        vo.setCategoryId(question.getCategoryId());
        vo.setQuestionNo(question.getQuestionNo());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setTitle(question.getTitle());
        vo.setAnswerSummary(firstNonBlank(question.getAnswerSummary(), shortenText(stripText(question.getQuestionStem()), 140)));
        vo.setKeywords(question.getKeywords());
        vo.setDifficulty(question.getDifficulty());
        vo.setViewCount(defaultInt(question.getViewCount()));
        vo.setStudyCount(defaultInt(question.getStudyCount()));
        vo.setPublishTime(question.getPublishTime());
        if (question.getCategoryId() != null) {
            StudyCategory category = studyCategoryService.getById(question.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        return vo;
    }

    private SearchStudyQuestionVO buildStudyQuestionSearchVOFromSource(JsonNode source) {
        SearchStudyQuestionVO vo = new SearchStudyQuestionVO();
        vo.setId(readLong(source, "id"));
        vo.setCategoryId(readLong(source, "categoryId"));
        vo.setCategoryName(readText(source, "categoryName"));
        vo.setQuestionNo(readInteger(source, "questionNo"));
        vo.setQuestionCode(readText(source, "questionCode"));
        vo.setTitle(readText(source, "title"));
        vo.setAnswerSummary(readText(source, "answerSummary"));
        vo.setKeywords(readText(source, "keywords"));
        vo.setDifficulty(readInteger(source, "difficulty"));
        vo.setViewCount(readInteger(source, "viewCount"));
        vo.setStudyCount(readInteger(source, "studyCount"));
        vo.setPublishTime(parseDateTime(readText(source, "publishTime")));
        return vo;
    }

    private UserVO buildUserSearchVOFromSource(JsonNode source) {
        UserVO vo = new UserVO();
        vo.setId(readLong(source, "id"));
        vo.setUsername(readText(source, "username"));
        vo.setNickname(readText(source, "nickname"));
        vo.setAvatar(readText(source, "avatar"));
        vo.setIntro(readText(source, "intro"));
        vo.setWebsite(readText(source, "website"));
        vo.setStatus(readInteger(source, "status"));
        vo.setUserLevel(readInteger(source, "userLevel"));
        vo.setVipLevel(readInteger(source, "vipLevel"));
        vo.setCreateTime(parseDateTime(readText(source, "createTime")));
        return vo;
    }

    private Map<String, Object> buildArticleDocument(Article article) {
        SearchArticleVO item = buildArticleSearchVO(article);
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("id", item.getId());
        document.put("categoryId", item.getCategoryId());
        document.put("categoryName", item.getCategoryName());
        document.put("title", item.getTitle());
        document.put("summary", item.getSummary());
        document.put("cover", item.getCover());
        document.put("content", shortenText(stripText(article.getContent()), 12000));
        document.put("viewCount", item.getViewCount());
        document.put("likeCount", item.getLikeCount());
        document.put("commentCount", item.getCommentCount());
        document.put("favoriteCount", item.getFavoriteCount());
        document.put("publishTime", formatDateTime(article.getPublishTime()));
        document.put("updateTime", formatDateTime(article.getUpdateTime()));
        document.put("status", article.getStatus());
        document.put("author", item.getAuthor());
        document.put("tags", item.getTags());
        document.put("tagNames", item.getTags() == null ? Collections.emptyList() : item.getTags().stream()
                .map(TagVO::getName)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList()));
        if (item.getAuthor() != null) {
            document.put("authorNickname", item.getAuthor().getNickname());
            document.put("authorUsername", item.getAuthor().getUsername());
        }
        return document;
    }

    private Map<String, Object> buildStudyQuestionDocument(StudyQuestion question) {
        SearchStudyQuestionVO item = buildStudyQuestionSearchVO(question);
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("id", item.getId());
        document.put("categoryId", item.getCategoryId());
        document.put("categoryName", item.getCategoryName());
        document.put("questionNo", item.getQuestionNo());
        document.put("questionCode", item.getQuestionCode());
        document.put("title", item.getTitle());
        document.put("questionStem", shortenText(stripText(question.getQuestionStem()), 16000));
        document.put("standardAnswer", shortenText(stripText(question.getStandardAnswer()), 16000));
        document.put("answerSummary", item.getAnswerSummary());
        document.put("keywords", item.getKeywords());
        document.put("difficulty", item.getDifficulty());
        document.put("viewCount", item.getViewCount());
        document.put("studyCount", item.getStudyCount());
        document.put("publishTime", formatDateTime(question.getPublishTime()));
        document.put("updateTime", formatDateTime(question.getUpdateTime()));
        document.put("status", question.getStatus());
        document.put("reviewStatus", question.getReviewStatus());
        return document;
    }

    private Map<String, Object> buildUserDocument(User user) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("id", user.getId());
        document.put("username", user.getUsername());
        document.put("nickname", user.getNickname());
        document.put("avatar", user.getAvatar());
        document.put("intro", shortenText(stripText(user.getIntro()), 4000));
        document.put("website", user.getWebsite());
        document.put("status", user.getStatus());
        document.put("userLevel", user.getUserLevel());
        document.put("vipLevel", user.getVipLevel());
        document.put("createTime", formatDateTime(user.getCreateTime()));
        document.put("updateTime", formatDateTime(user.getUpdateTime()));
        return document;
    }

    private List<TagVO> loadArticleTags(Long articleId) {
        List<ArticleTag> articleTags = articleTagService.list(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));
        if (articleTags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
        return tagService.listByIds(tagIds).stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setColor(tag.getColor());
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> buildArticleSearchQuery(String keyword) {
        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", keyword);
        multiMatch.put("fields", List.of(
                "title^6",
                "summary^4",
                "tagNames^4",
                "categoryName^2",
                "authorNickname^2",
                "authorUsername^1.5",
                "content^1.2"
        ));
        multiMatch.put("type", "best_fields");

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("must", List.of(Map.of("multi_match", multiMatch)));
        bool.put("filter", List.of(Map.of("term", Map.of("status", 1))));
        return Map.of("bool", bool);
    }

    private Map<String, Object> buildStudyQuestionSearchQuery(String keyword) {
        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", keyword);
        multiMatch.put("fields", List.of(
                "title^6",
                "keywords^5",
                "questionCode^3",
                "answerSummary^4",
                "categoryName^2",
                "questionStem^1.8",
                "standardAnswer^1.2"
        ));
        multiMatch.put("type", "best_fields");

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("must", List.of(Map.of("multi_match", multiMatch)));
        bool.put("filter", List.of(
                Map.of("term", Map.of("status", StudyConstants.STATUS_ENABLED)),
                Map.of("term", Map.of("reviewStatus", StudyConstants.REVIEW_PUBLISHED))
        ));
        return Map.of("bool", bool);
    }

    private Map<String, Object> buildUserSearchQuery(String keyword) {
        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", keyword);
        multiMatch.put("fields", List.of(
                "nickname^6",
                "username^4",
                "intro^2"
        ));
        multiMatch.put("type", "best_fields");

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("must", List.of(Map.of("multi_match", multiMatch)));
        bool.put("filter", List.of(Map.of("term", Map.of("status", 1))));
        return Map.of("bool", bool);
    }

    private Map<String, Object> buildHighlight(List<String> fields) {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        for (String field : fields) {
            fieldMap.put(field, new LinkedHashMap<>());
        }
        Map<String, Object> highlight = new LinkedHashMap<>();
        highlight.put("pre_tags", List.of("<mark class='search-highlight search-hit'>"));
        highlight.put("post_tags", List.of("</mark>"));
        highlight.put("fields", fieldMap);
        return highlight;
    }

    private void ensureIndices() throws IOException {
        ensureArticleIndex();
        ensureStudyQuestionIndex();
        ensureUserIndex();
    }

    private void ensureArticleIndex() throws IOException {
        if (articleIndexReady) {
            return;
        }
        synchronized (this) {
            if (articleIndexReady) {
                return;
            }
            ensureIndex(elasticsearchProperties.getArticleIndex(), buildArticleIndexMapping());
            articleIndexReady = true;
        }
    }

    private void ensureStudyQuestionIndex() throws IOException {
        if (studyQuestionIndexReady) {
            return;
        }
        synchronized (this) {
            if (studyQuestionIndexReady) {
                return;
            }
            ensureIndex(elasticsearchProperties.getStudyQuestionIndex(), buildStudyQuestionIndexMapping());
            studyQuestionIndexReady = true;
        }
    }

    private void ensureUserIndex() throws IOException {
        if (userIndexReady) {
            return;
        }
        synchronized (this) {
            if (userIndexReady) {
                return;
            }
            ensureIndex(elasticsearchProperties.getUserIndex(), buildUserIndexMapping());
            userIndexReady = true;
        }
    }

    private void ensureIndex(String indexName, Map<String, Object> mapping) throws IOException {
        if (indexExists(indexName)) {
            return;
        }
        executePlainRequest("PUT", "/" + indexName, mapping, true);
    }

    private boolean indexExists(String indexName) throws IOException {
        Request.Builder builder = baseRequestBuilder("/" + indexName).head();
        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            if (response.code() == 200) {
                return true;
            }
            if (response.code() == 404) {
                return false;
            }
            throw new IOException("检查索引失败，HTTP " + response.code() + ": " + readResponseBody(response));
        }
    }

    private Map<String, Object> buildArticleIndexMapping() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "long"));
        properties.put("categoryId", Map.of("type", "long"));
        properties.put("categoryName", textField());
        properties.put("title", textField());
        properties.put("summary", textField());
        properties.put("cover", Map.of("type", "keyword", "ignore_above", 512));
        properties.put("content", textField());
        properties.put("viewCount", Map.of("type", "integer"));
        properties.put("likeCount", Map.of("type", "integer"));
        properties.put("commentCount", Map.of("type", "integer"));
        properties.put("favoriteCount", Map.of("type", "integer"));
        properties.put("publishTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm"));
        properties.put("updateTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm"));
        properties.put("status", Map.of("type", "integer"));
        properties.put("authorNickname", textField());
        properties.put("authorUsername", textField());
        properties.put("tagNames", textField());
        properties.put("author", Map.of("properties", Map.of(
                "id", Map.of("type", "long"),
                "username", Map.of("type", "keyword", "ignore_above", 256),
                "nickname", Map.of("type", "keyword", "ignore_above", 256),
                "avatar", Map.of("type", "keyword", "ignore_above", 512),
                "vipLevel", Map.of("type", "integer")
        )));
        properties.put("tags", Map.of("properties", Map.of(
                "id", Map.of("type", "long"),
                "name", Map.of("type", "keyword", "ignore_above", 256),
                "color", Map.of("type", "keyword", "ignore_above", 64)
        )));
        return Map.of(
                "settings", Map.of("number_of_shards", 1, "number_of_replicas", 0),
                "mappings", Map.of("properties", properties)
        );
    }

    private Map<String, Object> buildStudyQuestionIndexMapping() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "long"));
        properties.put("categoryId", Map.of("type", "long"));
        properties.put("categoryName", textField());
        properties.put("questionNo", Map.of("type", "integer"));
        properties.put("questionCode", textField());
        properties.put("title", textField());
        properties.put("questionStem", textField());
        properties.put("standardAnswer", textField());
        properties.put("answerSummary", textField());
        properties.put("keywords", textField());
        properties.put("difficulty", Map.of("type", "integer"));
        properties.put("viewCount", Map.of("type", "integer"));
        properties.put("studyCount", Map.of("type", "integer"));
        properties.put("publishTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm"));
        properties.put("updateTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm"));
        properties.put("status", Map.of("type", "integer"));
        properties.put("reviewStatus", Map.of("type", "integer"));
        return Map.of(
                "settings", Map.of("number_of_shards", 1, "number_of_replicas", 0),
                "mappings", Map.of("properties", properties)
        );
    }

    private Map<String, Object> buildUserIndexMapping() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "long"));
        properties.put("username", textField());
        properties.put("nickname", textField());
        properties.put("avatar", Map.of("type", "keyword", "ignore_above", 512));
        properties.put("intro", textField());
        properties.put("website", Map.of("type", "keyword", "ignore_above", 512));
        properties.put("status", Map.of("type", "integer"));
        properties.put("userLevel", Map.of("type", "integer"));
        properties.put("vipLevel", Map.of("type", "integer"));
        properties.put("createTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm"));
        properties.put("updateTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm"));
        return Map.of(
                "settings", Map.of("number_of_shards", 1, "number_of_replicas", 0),
                "mappings", Map.of("properties", properties)
        );
    }

    private Map<String, Object> textField() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "text");
        field.put("analyzer", elasticsearchProperties.getAnalyzer());
        field.put("search_analyzer", elasticsearchProperties.getSearchAnalyzer());
        return field;
    }

    private void upsertDocument(String indexName, String id, Map<String, Object> document) throws IOException {
        executePlainRequest("PUT", "/" + indexName + "/_doc/" + id, document, true);
    }

    private void deleteDocument(String indexName, String id) throws IOException {
        Request.Builder builder = baseRequestBuilder("/" + indexName + "/_doc/" + id).delete();
        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            if (response.code() == 200 || response.code() == 202 || response.code() == 404) {
                return;
            }
            throw new IOException("删除索引文档失败，HTTP " + response.code() + ": " + readResponseBody(response));
        }
    }

    private JsonNode executeJsonRequest(String method, String path, Object body) throws IOException {
        return objectMapper.readTree(executePlainRequest(method, path, body, false));
    }

    private String executePlainRequest(String method, String path, Object body, boolean allowCreate) throws IOException {
        RequestBody requestBody = body == null ? null : RequestBody.create(objectMapper.writeValueAsString(body), JSON_MEDIA_TYPE);
        Request.Builder builder = baseRequestBuilder(path);
        builder.method(method, requestBody);
        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            String responseText = readResponseBody(response);
            if (response.isSuccessful()) {
                return responseText;
            }
            if (allowCreate && response.code() == 400 && responseText.contains("resource_already_exists_exception")) {
                return responseText;
            }
            throw new IOException("请求 Elasticsearch 失败，HTTP " + response.code() + ": " + responseText);
        }
    }

    private Request.Builder baseRequestBuilder(String path) {
        Request.Builder builder = new Request.Builder().url(buildUrl(path));
        if (StringUtils.hasText(elasticsearchProperties.getUsername())) {
            builder.header("Authorization", Credentials.basic(
                    elasticsearchProperties.getUsername(),
                    StrUtil.nullToEmpty(elasticsearchProperties.getPassword())
            ));
        }
        builder.header("Content-Type", "application/json");
        return builder;
    }

    private String buildUrl(String path) {
        String endpoint = elasticsearchProperties.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + path;
    }

    private String readResponseBody(Response response) throws IOException {
        return response.body() == null ? "" : response.body().string();
    }

    private boolean isElasticsearchEnabled() {
        return elasticsearchProperties.isEnabled() && StringUtils.hasText(elasticsearchProperties.getEndpoint());
    }

    private boolean isSearchableArticle(Article article) {
        return article != null && Objects.equals(article.getStatus(), 1);
    }

    private boolean isSearchableStudyQuestion(StudyQuestion question) {
        return question != null
                && Objects.equals(question.getStatus(), StudyConstants.STATUS_ENABLED)
                && Objects.equals(question.getReviewStatus(), StudyConstants.REVIEW_PUBLISHED);
    }

    private boolean isSearchableUser(User user) {
        return user != null && Objects.equals(user.getStatus(), 1);
    }

    private String normalizeScope(String scope) {
        if (!StringUtils.hasText(scope)) {
            return "all";
        }
        String normalized = scope.trim().toLowerCase();
        Set<String> allowed = new LinkedHashSet<>(List.of("all", "article", "study", "user"));
        return allowed.contains(normalized) ? normalized : "all";
    }

    private String stripText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String plainText = HtmlUtil.cleanHtmlTag(text);
        plainText = plainText.replaceAll("`{1,3}", " ")
                .replaceAll("#{1,6}", " ")
                .replaceAll("\\*|>|\\-|\\|", " ")
                .replaceAll("\\[(.*?)]\\((.*?)\\)", "$1")
                .replaceAll("\\s+", " ")
                .trim();
        return plainText;
    }

    private String shortenText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String formatDateTime(LocalDateTime time) {
        return time == null ? null : time.toString();
    }

    private String readText(JsonNode source, String field) {
        JsonNode value = source.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private Long readLong(JsonNode source, String field) {
        JsonNode value = source.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asLong();
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? Long.parseLong(text) : null;
    }

    private Integer readInteger(JsonNode source, String field) {
        JsonNode value = source.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asInt();
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? Integer.parseInt(text) : null;
    }

    private void recordHotKeyword(String keyword) {
        String normalizedKeyword = normalizeHotKeyword(keyword);
        if (!StringUtils.hasText(normalizedKeyword)) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(HOT_SEARCH_ZONE);
        String key = buildHotSearchKey(now.toLocalDate());
        redisService.zIncrementScore(key, normalizedKeyword, 1D);
        redisService.expire(key, getSecondsUntilMidnight(now), TimeUnit.SECONDS);
    }

    private String normalizeHotKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return keyword.trim().replaceAll("\\s+", " ");
    }

    private String buildHotSearchKey(LocalDate date) {
        return RedisService.CACHE_SEARCH_HOT + date.format(HOT_SEARCH_KEY_FORMATTER);
    }

    private long getSecondsUntilMidnight(ZonedDateTime now) {
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(HOT_SEARCH_ZONE);
        long seconds = nextMidnight.toEpochSecond() - now.toEpochSecond();
        return Math.max(seconds, 1L);
    }

    private LocalDateTime parseDateTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        for (DateTimeFormatter formatter : SUPPORTED_DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // 兼容旧索引和新索引里的不同时间格式
            }
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MIN);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String readHighlight(JsonNode highlightNode, String field) {
        JsonNode fieldNode = highlightNode.path(field);
        if (fieldNode.isArray() && fieldNode.size() > 0) {
            return fieldNode.get(0).asText();
        }
        return null;
    }
}
