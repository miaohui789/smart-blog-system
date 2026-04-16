package com.blog.service.impl;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.constant.StudyConstants;
import com.blog.common.result.PageResult;
import com.blog.config.AiKnowledgeProperties;
import com.blog.dto.response.AiKnowledgePayloadVO;
import com.blog.dto.response.AiKnowledgeSourceVO;
import com.blog.dto.response.SearchAllVO;
import com.blog.dto.response.SearchArticleVO;
import com.blog.dto.response.SearchStudyQuestionVO;
import com.blog.entity.Article;
import com.blog.entity.StudyCategory;
import com.blog.entity.StudyQuestion;
import com.blog.entity.StudyUserQuestionProgress;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.StudyCategoryMapper;
import com.blog.mapper.StudyQuestionMapper;
import com.blog.mapper.StudyUserQuestionProgressMapper;
import com.blog.service.AiKnowledgeService;
import com.blog.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl implements AiKnowledgeService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int SEARCH_CANDIDATE_MULTIPLIER = 4;
    private static final int SEARCH_MIN_CANDIDATE_SIZE = 8;
    private static final double ARTICLE_ABSOLUTE_SCORE_THRESHOLD = 7.2D;
    private static final double QUESTION_ABSOLUTE_SCORE_THRESHOLD = 7.8D;
    private static final double RELATIVE_SCORE_THRESHOLD_RATIO = 0.45D;
    private static final double STRONG_TERM_MISS_PENALTY = 8.5D;
    private static final Set<String> GENERIC_MATCH_TERMS = Set.of(
            "java", "spring", "springboot", "springmvc", "mvc", "boot",
            "mysql", "redis", "nginx", "vue", "jdk", "jvm", "mybatis",
            "mybatisplus", "maven", "git", "docker", "linux"
    );
    private static final String[] TOPIC_SUFFIXES = {
            "详解", "解析", "介绍", "总结", "原理", "流程", "实践", "教程", "指南", "配置", "用法"
    };
    private static final Set<String> QUERY_STOP_WORDS = Set.of(
            "我", "我们", "我的", "站内", "结合", "一下", "一题", "一道", "几道", "几题",
            "我要", "我想", "想要", "帮我", "给我", "请", "测试", "测试下", "测试一下",
            "测下", "测一下", "考考我", "来几道", "来几题", "来道题", "随机来几道",
            "随机来几题", "随机抽几道", "随机出几道", "随机", "抽题", "出题", "练习", "练练",
            "说说", "讲讲", "讲一下", "介绍", "介绍下", "介绍一下", "什么", "一下子"
    );

    private final AiKnowledgeProperties aiKnowledgeProperties;
    private final SearchService searchService;
    private final ArticleMapper articleMapper;
    private final StudyCategoryMapper studyCategoryMapper;
    private final StudyQuestionMapper studyQuestionMapper;
    private final StudyUserQuestionProgressMapper progressMapper;

    @Override
    public AiKnowledgePayloadVO buildKnowledgeContext(Long userId, String question) {
        if (!aiKnowledgeProperties.isEnabled() || !StringUtils.hasText(question)) {
            return null;
        }
        try {
            String normalizedQuestion = question.trim();
            boolean articleIntent = hasAnyKeyword(normalizedQuestion, "文章", "博文", "博客", "站内文章", "推荐几篇", "推荐文章", "阅读");
            boolean studyIntent = hasAnyKeyword(normalizedQuestion, "面试题", "八股", "题库", "刷题", "复习", "学习记录", "错题", "收藏题");
            boolean randomQuestionIntent = studyIntent && hasAnyKeyword(normalizedQuestion,
                    "随机", "来几道", "来几题", "抽几道", "出几道", "测试下", "测试一下", "测一下", "考考我");
            boolean projectKnowledgeIntent = articleIntent
                    || studyIntent
                    || hasAnyKeyword(normalizedQuestion, "站内", "项目", "数据库", "知识库", "内容库", "我的文章", "我的题库");
            QueryProfile queryProfile = buildQueryProfile(normalizedQuestion, articleIntent, studyIntent, projectKnowledgeIntent);
            int limit = Math.max(aiKnowledgeProperties.getArticleLimit(), aiKnowledgeProperties.getStudyQuestionLimit());
            SearchBundle searchBundle = searchKnowledge(queryProfile, limit);
            List<SearchArticleVO> articleHits = limitList(searchBundle.getArticleHits(), aiKnowledgeProperties.getArticleLimit());
            List<SearchStudyQuestionVO> questionHits = limitList(searchBundle.getQuestionHits(), aiKnowledgeProperties.getStudyQuestionLimit());
            boolean randomQuestionFallback = false;
            if (questionHits.isEmpty() && randomQuestionIntent) {
                questionHits = loadRandomStudyQuestionHits(aiKnowledgeProperties.getStudyQuestionLimit());
                randomQuestionFallback = !questionHits.isEmpty();
            }
            if (articleHits.isEmpty() && questionHits.isEmpty() && !projectKnowledgeIntent) {
                return null;
            }

            Map<Long, Article> articleMap = loadArticleMap(articleHits);
            Map<Long, StudyQuestion> questionMap = loadQuestionMap(questionHits);
            Map<Long, StudyUserQuestionProgress> progressMap = loadProgressMap(userId, questionMap.keySet());
            AiKnowledgePayloadVO payload = new AiKnowledgePayloadVO();

            StringBuilder context = new StringBuilder(4096);
            context.append("你是 Smart Blog System 的站内智能助手。")
                    .append("以下内容来自当前项目的文章库、面试题库和用户学习记录。")
                    .append("你当前已经拿到了站内检索结果。")
                    .append("严禁再说“我无法访问你的站内文章库/数据库/本地文件/项目内容”这类表述。");
            context.append("\n回答规则：")
                    .append("\n1. 只能引用下面检索结果里真实出现的文章标题、题目标题、分类、学习状态和统计信息。")
                    .append("\n2. 严禁编造不存在的文章标题、题目标题、专栏名、学习记录或数据库内容。")
                    .append("\n3. 如果某类内容没有检索到，就明确说“站内未检索到相关内容”，不要伪造推荐列表。")
                    .append("\n4. 如果用户要求推荐文章，你只能推荐下面命中的文章；如果文章命中为空，就直接说明未检索到相关文章。")
                    .append("\n5. 如果用户要求题库总结或复习建议，你只能基于下面命中的题目和学习记录回答。")
                    .append("\n6. 如果检索结果不足以支撑完整结论，可以在说明“站内未检索到足够直接的内容”后，再补充通用建议，但必须先说明站内命中情况。");
            context.append("\n本次提问：").append(normalizedQuestion);
            context.append("\n本次检索使用的查询词：").append(searchBundle.getSummary());
            context.append("\n本次站内检索命中统计：文章 ").append(articleHits.size()).append(" 篇，面试题 ").append(questionHits.size()).append(" 道。");
            if (randomQuestionFallback) {
                context.append("\n当前没有检索到与提问文本直接匹配的面试题，已从站内已发布题库中随机抽取若干题目供测试使用。");
            }
            if (articleIntent && articleHits.isEmpty()) {
                context.append("\n用户本次明确想看文章推荐，但当前站内文章库没有检索到直接相关的文章。回答时必须明确说明这一点。");
            }
            if (studyIntent && questionHits.isEmpty() && !randomQuestionFallback) {
                context.append("\n用户本次明确想看题库/复习内容，但当前站内题库没有检索到直接相关的题目。回答时必须明确说明这一点。");
            }
            context.append("\n如果用户的问题与求职、复习、面试准备有关，请优先输出：相关面试题、推荐阅读文章、建议复习顺序、薄弱点提醒。");

            if (!questionHits.isEmpty()) {
                context.append(randomQuestionFallback ? "\n\n【站内随机抽取面试题】" : "\n\n【面试题库检索结果】");
                int index = 1;
                for (SearchStudyQuestionVO item : questionHits) {
                    StudyQuestion questionDetail = questionMap.get(item.getId());
                    if (questionDetail == null) {
                        continue;
                    }
                    context.append("\n").append(index++).append(". 题目ID: ").append(questionDetail.getId());
                    context.append("\n标题: ").append(defaultText(questionDetail.getTitle(), item.getTitle()));
                    context.append("\n分类: ").append(defaultText(item.getCategoryName(), "未分类"));
                    context.append("\n关键词: ").append(defaultText(questionDetail.getKeywords(), item.getKeywords(), "无"));
                    context.append("\n题干: ").append(shorten(cleanText(questionDetail.getQuestionStem()), aiKnowledgeProperties.getQuestionStemMaxLength()));
                    context.append("\n标准答案: ").append(shorten(cleanText(questionDetail.getStandardAnswer()), aiKnowledgeProperties.getQuestionAnswerMaxLength()));
                    context.append("\n答案摘要: ").append(defaultText(item.getAnswerSummary(), questionDetail.getAnswerSummary(), "无"));
                    context.append("\n难度: ").append(formatDifficulty(questionDetail.getDifficulty()));
                    context.append("\n发布时间: ").append(formatDateTime(questionDetail.getPublishTime()));

                    StudyUserQuestionProgress progress = progressMap.get(questionDetail.getId());
                    if (progress != null) {
                        context.append("\n当前用户学习情况: 学习状态=").append(formatStudyStatus(progress.getStudyStatus()))
                                .append("，收藏=").append(Objects.equals(progress.getIsFavorite(), 1) ? "是" : "否")
                                .append("，错题=").append(Objects.equals(progress.getIsWrongQuestion(), 1) ? "是" : "否")
                                .append("，学习次数=").append(defaultInt(progress.getStudyCount()))
                                .append("，最近得分=").append(progress.getLastScore() == null ? "暂无" : progress.getLastScore().toPlainString())
                                .append("，最近学习时间=").append(formatDateTime(progress.getLastStudyTime()));
                    }
                    payload.getSources().add(buildStudyQuestionSource(questionDetail, item, progress));
                }
            }

            if (!articleHits.isEmpty()) {
                context.append("\n\n【文章库检索结果】");
                int index = 1;
                for (SearchArticleVO item : articleHits) {
                    Article article = articleMap.get(item.getId());
                    if (article == null) {
                        continue;
                    }
                    context.append("\n").append(index++).append(". 文章ID: ").append(article.getId());
                    context.append("\n标题: ").append(defaultText(article.getTitle(), item.getTitle()));
                    context.append("\n分类: ").append(defaultText(item.getCategoryName(), article.getCategoryName(), "未分类"));
                    context.append("\n摘要: ").append(defaultText(article.getSummary(), item.getSummary(), "无"));
                    context.append("\n正文片段: ").append(shorten(extractArticleText(article), aiKnowledgeProperties.getArticleContentMaxLength()));
                    context.append("\n热度数据: 浏览=").append(defaultInt(article.getViewCount()))
                            .append("，点赞=").append(defaultInt(article.getLikeCount()))
                            .append("，评论=").append(defaultInt(article.getCommentCount()))
                            .append("，收藏=").append(defaultInt(article.getFavoriteCount()));
                    context.append("\n发布时间: ").append(formatDateTime(article.getPublishTime()));
                    payload.getSources().add(buildArticleSource(article, item));
                }
            }

            payload.setPromptContext(context.toString());
            return payload;
        } catch (Exception e) {
            log.warn("构建AI项目知识上下文失败: {}", e.getMessage());
            return null;
        }
    }

    private SearchBundle searchKnowledge(QueryProfile profile, int limit) {
        LinkedHashMap<Long, RankedArticleHit> articleMap = new LinkedHashMap<>();
        LinkedHashMap<Long, RankedStudyQuestionHit> questionMap = new LinkedHashMap<>();
        int currentLimit = Math.max(Math.max(limit, 1) * SEARCH_CANDIDATE_MULTIPLIER, SEARCH_MIN_CANDIDATE_SIZE);
        List<String> executedQueries = profile.getQueries().isEmpty() ? List.of(profile.getQuestion()) : profile.getQueries();
        for (int i = 0; i < executedQueries.size(); i++) {
            String query = executedQueries.get(i);
            SearchAllVO searchResult = searchService.search(query, 1, currentLimit, false);
            mergeArticleHits(articleMap, searchResult != null ? safePageList(searchResult.getArticles()) : Collections.emptyList(), i);
            mergeQuestionHits(questionMap, searchResult != null ? safePageList(searchResult.getStudyQuestions()) : Collections.emptyList(), i);
        }
        return new SearchBundle(
                rerankArticleHits(profile, articleMap.values()),
                rerankQuestionHits(profile, questionMap.values()),
                String.join(" / ", executedQueries)
        );
    }

    private QueryProfile buildQueryProfile(String question,
                                           boolean articleIntent,
                                           boolean studyIntent,
                                           boolean projectKnowledgeIntent) {
        List<String> queries = buildSearchQueries(question);
        Set<String> terms = new LinkedHashSet<>();
        terms.add(normalizeForMatch(question));
        queries.stream()
                .map(this::normalizeForMatch)
                .filter(StringUtils::hasText)
                .forEach(terms::add);
        queries.stream()
                .flatMap(item -> extractKeywordQueries(item).stream())
                .map(this::normalizeForMatch)
                .filter(StringUtils::hasText)
                .forEach(terms::add);
        queries.stream()
                .flatMap(item -> extractMeaningfulTerms(item).stream())
                .filter(StringUtils::hasText)
                .forEach(terms::add);

        List<String> sortedTerms = terms.stream()
                .filter(item -> item.length() >= 2)
                .sorted((left, right) -> Integer.compare(right.length(), left.length()))
                .collect(Collectors.toList());
        List<String> strongTerms = sortedTerms.stream()
                .filter(this::isStrongMatchTerm)
                .collect(Collectors.toList());
        return new QueryProfile(question, queries, sortedTerms, strongTerms, articleIntent, studyIntent, projectKnowledgeIntent);
    }

    private List<String> buildSearchQueries(String question) {
        if (!StringUtils.hasText(question)) {
            return Collections.emptyList();
        }
        Set<String> queries = new LinkedHashSet<>();
        String normalized = question.trim();
        queries.add(normalized);

        String simplified = normalized
                .replaceAll("(?i)结合我(?:的)?站内文章", " ")
                .replaceAll("(?i)结合我(?:的)?文章库", " ")
                .replaceAll("(?i)结合我(?:的)?题库", " ")
                .replaceAll("(?i)结合我(?:的)?学习记录", " ")
                .replaceAll("(?i)结合我(?:的)?站内内容", " ")
                .replaceAll("(?i)推荐几篇", " ")
                .replaceAll("(?i)推荐一些", " ")
                .replaceAll("(?i)适合复习", " ")
                .replaceAll("(?i)适合学习", " ")
                .replaceAll("(?i)给我推荐", " ")
                .replaceAll("(?i)帮我推荐", " ")
                .replaceAll("(?i)相关的?(文章|内容|题目|资料)", " ")
                .replaceAll("(?i)(内容|文章|题目|资料|知识点|一下)$", " ")
                .replaceAll("[，。！？、,.!?:：；;（）()\"“”‘’]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (StringUtils.hasText(simplified) && simplified.length() >= 2 && !normalized.equals(simplified)) {
            queries.add(simplified);
        }

        String topic = simplified
                .replaceAll("(?i)^(关于|结合|针对|对于|有关|围绕)", "")
                .replaceAll("(?i)(怎么|如何|有哪些|是什么|讲一下|总结一下)$", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (StringUtils.hasText(topic) && topic.length() >= 2 && !queries.contains(topic)) {
            queries.add(topic);
        }
        queries.addAll(extractKeywordQueries(topic));
        return new ArrayList<>(queries);
    }

    private List<String> extractKeywordQueries(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        String normalized = text
                .replaceAll("(?i)站内面试题", " 面试题 ")
                .replaceAll("(?i)站内文章", " 文章 ")
                .replaceAll("[，。！？、,.!?:：；;（）()\"“”‘’]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (!StringUtils.hasText(normalized)) {
            return Collections.emptyList();
        }
        Set<String> queries = new LinkedHashSet<>();
        for (String token : normalized.split("\\s+")) {
            String candidate = token.trim();
            if (!StringUtils.hasText(candidate) || candidate.length() < 2 || QUERY_STOP_WORDS.contains(candidate)) {
                continue;
            }
            queries.add(candidate);
        }
        return new ArrayList<>(queries);
    }

    private List<String> extractMeaningfulTerms(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        String cleaned = cleanText(text).toLowerCase()
                .replaceAll("[^\\p{IsHan}a-z0-9.]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (!StringUtils.hasText(cleaned)) {
            return Collections.emptyList();
        }
        Set<String> terms = new LinkedHashSet<>();
        String[] tokens = cleaned.split("\\s+");
        StringBuilder englishPhrase = new StringBuilder();
        for (String token : tokens) {
            String normalized = normalizeForMatch(token);
            if (!StringUtils.hasText(normalized) || normalized.length() < 2 || QUERY_STOP_WORDS.contains(normalized)) {
                continue;
            }
            terms.add(normalized);
            String trimmedTopic = trimTopicSuffix(normalized);
            if (!normalized.equals(trimmedTopic) && StringUtils.hasText(trimmedTopic) && trimmedTopic.length() >= 2) {
                terms.add(trimmedTopic);
            }
            if (token.matches("[a-z]+")) {
                englishPhrase.append(token);
                continue;
            }
            if (englishPhrase.length() >= 4) {
                terms.add(englishPhrase.toString());
            }
            englishPhrase.setLength(0);
        }
        if (englishPhrase.length() >= 4) {
            terms.add(englishPhrase.toString());
        }
        return new ArrayList<>(terms);
    }

    private List<SearchStudyQuestionVO> loadRandomStudyQuestionHits(int limit) {
        int currentLimit = Math.max(limit, 1);
        List<StudyQuestion> randomQuestions = studyQuestionMapper.selectList(new LambdaQueryWrapper<StudyQuestion>()
                .eq(StudyQuestion::getStatus, StudyConstants.STATUS_ENABLED)
                .eq(StudyQuestion::getReviewStatus, StudyConstants.REVIEW_PUBLISHED)
                .last("ORDER BY RAND() LIMIT " + currentLimit));
        if (randomQuestions == null || randomQuestions.isEmpty()) {
            return Collections.emptyList();
        }

        if (randomQuestions.size() > 1) {
            Collections.shuffle(randomQuestions, ThreadLocalRandom.current());
        }
        Map<Long, StudyCategory> categoryMap = studyCategoryMapper.selectBatchIds(randomQuestions.stream()
                        .map(StudyQuestion::getCategoryId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(StudyCategory::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        return randomQuestions.stream()
                .limit(currentLimit)
                .map(question -> buildRandomStudyQuestionHit(question, categoryMap.get(question.getCategoryId())))
                .collect(Collectors.toList());
    }

    private SearchStudyQuestionVO buildRandomStudyQuestionHit(StudyQuestion question, StudyCategory category) {
        SearchStudyQuestionVO vo = new SearchStudyQuestionVO();
        vo.setId(question.getId());
        vo.setCategoryId(question.getCategoryId());
        vo.setCategoryName(category != null ? category.getCategoryName() : null);
        vo.setQuestionNo(question.getQuestionNo());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setTitle(question.getTitle());
        vo.setAnswerSummary(question.getAnswerSummary());
        vo.setKeywords(question.getKeywords());
        vo.setDifficulty(question.getDifficulty());
        vo.setViewCount(defaultInt(question.getViewCount()));
        vo.setStudyCount(defaultInt(question.getStudyCount()));
        vo.setPublishTime(question.getPublishTime());
        return vo;
    }

    private List<SearchArticleVO> rerankArticleHits(QueryProfile profile, java.util.Collection<RankedArticleHit> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScoredArticleHit> scoredHits = candidates.stream()
                .map(item -> buildScoredArticleHit(profile, item))
                .sorted((left, right) -> compareScore(right.getScore(), left.getScore(), right.getSearchItem().getPublishTime(), left.getSearchItem().getPublishTime()))
                .collect(Collectors.toList());
        return filterArticleHits(profile, scoredHits);
    }

    private List<SearchStudyQuestionVO> rerankQuestionHits(QueryProfile profile, java.util.Collection<RankedStudyQuestionHit> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScoredStudyQuestionHit> scoredHits = candidates.stream()
                .map(item -> buildScoredStudyQuestionHit(profile, item))
                .sorted((left, right) -> compareScore(right.getScore(), left.getScore(), right.getSearchItem().getPublishTime(), left.getSearchItem().getPublishTime()))
                .collect(Collectors.toList());
        return filterQuestionHits(profile, scoredHits);
    }

    private int compareScore(double leftScore, double rightScore, LocalDateTime leftTime, LocalDateTime rightTime) {
        int scoreCompare = Double.compare(leftScore, rightScore);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        if (leftTime == null && rightTime == null) {
            return 0;
        }
        if (leftTime == null) {
            return -1;
        }
        if (rightTime == null) {
            return 1;
        }
        return leftTime.compareTo(rightTime);
    }

    private List<SearchArticleVO> filterArticleHits(QueryProfile profile, List<ScoredArticleHit> scoredHits) {
        if (scoredHits.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScoredArticleHit> candidateHits = applyArticleStrongTermGate(profile, scoredHits);
        if (candidateHits.isEmpty()) {
            return Collections.emptyList();
        }
        double threshold = resolveThreshold(candidateHits.stream().map(ScoredArticleHit::getScore).collect(Collectors.toList()), ARTICLE_ABSOLUTE_SCORE_THRESHOLD);
        List<SearchArticleVO> filtered = candidateHits.stream()
                .filter(item -> item.getScore() >= threshold)
                .map(ScoredArticleHit::getSearchItem)
                .collect(Collectors.toList());
        if (!filtered.isEmpty()) {
            return filtered;
        }
        if (candidateHits.get(0).getScore() >= ARTICLE_ABSOLUTE_SCORE_THRESHOLD * 0.85D) {
            return List.of(candidateHits.get(0).getSearchItem());
        }
        return Collections.emptyList();
    }

    private List<SearchStudyQuestionVO> filterQuestionHits(QueryProfile profile, List<ScoredStudyQuestionHit> scoredHits) {
        if (scoredHits.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScoredStudyQuestionHit> candidateHits = applyQuestionStrongTermGate(profile, scoredHits);
        if (candidateHits.isEmpty()) {
            return Collections.emptyList();
        }
        double threshold = resolveThreshold(candidateHits.stream().map(ScoredStudyQuestionHit::getScore).collect(Collectors.toList()), QUESTION_ABSOLUTE_SCORE_THRESHOLD);
        List<SearchStudyQuestionVO> filtered = candidateHits.stream()
                .filter(item -> item.getScore() >= threshold)
                .map(ScoredStudyQuestionHit::getSearchItem)
                .collect(Collectors.toList());
        if (!filtered.isEmpty()) {
            return filtered;
        }
        if (candidateHits.get(0).getScore() >= QUESTION_ABSOLUTE_SCORE_THRESHOLD * 0.85D) {
            return List.of(candidateHits.get(0).getSearchItem());
        }
        return Collections.emptyList();
    }

    private List<ScoredArticleHit> applyArticleStrongTermGate(QueryProfile profile, List<ScoredArticleHit> scoredHits) {
        if (scoredHits.isEmpty() || profile.getStrongTerms().isEmpty()) {
            return scoredHits;
        }
        List<ScoredArticleHit> exactQuestionHits = scoredHits.stream()
                .filter(ScoredArticleHit::isExactQuestionMatched)
                .collect(Collectors.toList());
        if (!exactQuestionHits.isEmpty()) {
            return exactQuestionHits;
        }
        List<ScoredArticleHit> strongHits = scoredHits.stream()
                .filter(item -> item.getStrongTermMatchCount() > 0)
                .collect(Collectors.toList());
        if (!strongHits.isEmpty()) {
            return strongHits;
        }
        return Collections.emptyList();
    }

    private List<ScoredStudyQuestionHit> applyQuestionStrongTermGate(QueryProfile profile, List<ScoredStudyQuestionHit> scoredHits) {
        if (scoredHits.isEmpty() || profile.getStrongTerms().isEmpty()) {
            return scoredHits;
        }
        List<ScoredStudyQuestionHit> exactQuestionHits = scoredHits.stream()
                .filter(ScoredStudyQuestionHit::isExactQuestionMatched)
                .collect(Collectors.toList());
        if (!exactQuestionHits.isEmpty()) {
            return exactQuestionHits;
        }
        List<ScoredStudyQuestionHit> strongHits = scoredHits.stream()
                .filter(item -> item.getStrongTermMatchCount() > 0)
                .collect(Collectors.toList());
        if (!strongHits.isEmpty()) {
            return strongHits;
        }
        return Collections.emptyList();
    }

    private ScoredArticleHit buildScoredArticleHit(QueryProfile profile, RankedArticleHit candidate) {
        SearchArticleVO item = candidate.getSearchItem();
        String title = normalizeForMatch(defaultText(item.getTitleHighlight(), item.getTitle()));
        String summary = normalizeForMatch(defaultText(item.getSummaryHighlight(), item.getSummary()));
        String category = normalizeForMatch(item.getCategoryName());
        String tags = normalizeForMatch(item.getTags() == null ? "" : item.getTags().stream()
                .map(tag -> tag == null ? "" : tag.getName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" ")));
        String question = normalizeForMatch(profile.getQuestion());
        boolean exactQuestionMatched = (StringUtils.hasText(question) && title.contains(question))
                || (StringUtils.hasText(question) && summary.contains(question));
        int strongTermMatchCount = countStrongTermMatches(profile.getStrongTerms(), title, summary, tags, category);
        return new ScoredArticleHit(item, scoreArticleHit(profile, candidate), strongTermMatchCount, exactQuestionMatched);
    }

    private ScoredStudyQuestionHit buildScoredStudyQuestionHit(QueryProfile profile, RankedStudyQuestionHit candidate) {
        SearchStudyQuestionVO item = candidate.getSearchItem();
        String title = normalizeForMatch(defaultText(item.getTitleHighlight(), item.getTitle()));
        String answerSummary = normalizeForMatch(defaultText(item.getAnswerSummaryHighlight(), item.getAnswerSummary()));
        String keywords = normalizeForMatch(item.getKeywords());
        String category = normalizeForMatch(item.getCategoryName());
        String questionCode = normalizeForMatch(item.getQuestionCode());
        String question = normalizeForMatch(profile.getQuestion());
        boolean exactQuestionMatched = (StringUtils.hasText(question) && title.contains(question))
                || (StringUtils.hasText(question) && answerSummary.contains(question));
        int strongTermMatchCount = countStrongTermMatches(profile.getStrongTerms(), title, answerSummary, keywords, category, questionCode);
        return new ScoredStudyQuestionHit(item, scoreQuestionHit(profile, candidate), strongTermMatchCount, exactQuestionMatched);
    }

    private int countStrongTermMatches(List<String> strongTerms, String... fields) {
        if (strongTerms == null || strongTerms.isEmpty() || fields == null || fields.length == 0) {
            return 0;
        }
        int matchCount = 0;
        for (String term : strongTerms) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            boolean matched = false;
            for (String field : fields) {
                if (StringUtils.hasText(field) && field.contains(term)) {
                    matched = true;
                    break;
                }
            }
            if (matched) {
                matchCount++;
            }
        }
        return matchCount;
    }

    private double resolveThreshold(List<Double> scores, double absoluteThreshold) {
        if (scores == null || scores.isEmpty()) {
            return absoluteThreshold;
        }
        double bestScore = scores.get(0);
        return Math.max(absoluteThreshold, bestScore * RELATIVE_SCORE_THRESHOLD_RATIO);
    }

    private double scoreArticleHit(QueryProfile profile, RankedArticleHit candidate) {
        SearchArticleVO item = candidate.getSearchItem();
        String title = normalizeForMatch(defaultText(item.getTitleHighlight(), item.getTitle()));
        String summary = normalizeForMatch(defaultText(item.getSummaryHighlight(), item.getSummary()));
        String category = normalizeForMatch(item.getCategoryName());
        String tags = normalizeForMatch(item.getTags() == null ? "" : item.getTags().stream()
                .map(tag -> tag == null ? "" : tag.getName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" ")));
        String question = normalizeForMatch(profile.getQuestion());
        boolean matchedStrongTerm = false;

        double score = candidate.getHitCount() * 0.8D;
        score += Math.max(0, 4 - candidate.getFirstQueryIndex()) * 0.6D;
        if (profile.isArticleIntent()) {
            score += 2.2D;
        }
        if (StringUtils.hasText(item.getTitleHighlight())) {
            score += 1.8D;
        }
        if (StringUtils.hasText(item.getSummaryHighlight())) {
            score += 1.2D;
        }
        if (StringUtils.hasText(question) && StringUtils.hasText(title) && title.contains(question)) {
            score += 9.5D;
        }
        if (StringUtils.hasText(question) && StringUtils.hasText(summary) && summary.contains(question)) {
            score += 4.2D;
        }

        int matchedTerms = 0;
        for (String term : profile.getTerms()) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            boolean currentMatched = false;
            if (title.contains(term)) {
                score += 4.6D;
                currentMatched = true;
            }
            if (summary.contains(term)) {
                score += 2.4D;
                currentMatched = true;
            }
            if (tags.contains(term)) {
                score += 2.2D;
                currentMatched = true;
            }
            if (category.contains(term)) {
                score += 1.1D;
                currentMatched = true;
            }
            if (currentMatched) {
                matchedTerms++;
            }
        }
        for (String term : profile.getStrongTerms()) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            if (title.contains(term)) {
                score += 6.0D;
                matchedStrongTerm = true;
                continue;
            }
            if (summary.contains(term) || tags.contains(term) || category.contains(term)) {
                score += 3.0D;
                matchedStrongTerm = true;
            }
        }
        if (!profile.getStrongTerms().isEmpty()
                && !matchedStrongTerm
                && !(StringUtils.hasText(question) && title.contains(question))
                && !(StringUtils.hasText(question) && summary.contains(question))) {
            score -= STRONG_TERM_MISS_PENALTY;
        }
        score += Math.min(matchedTerms, 4) * 0.8D;
        score += Math.min(defaultInt(item.getViewCount()) / 200.0D, 0.8D);
        score += Math.min(defaultInt(item.getLikeCount()) / 80.0D, 0.6D);
        return score;
    }

    private double scoreQuestionHit(QueryProfile profile, RankedStudyQuestionHit candidate) {
        SearchStudyQuestionVO item = candidate.getSearchItem();
        String title = normalizeForMatch(defaultText(item.getTitleHighlight(), item.getTitle()));
        String answerSummary = normalizeForMatch(defaultText(item.getAnswerSummaryHighlight(), item.getAnswerSummary()));
        String keywords = normalizeForMatch(item.getKeywords());
        String category = normalizeForMatch(item.getCategoryName());
        String questionCode = normalizeForMatch(item.getQuestionCode());
        String question = normalizeForMatch(profile.getQuestion());
        boolean matchedStrongTerm = false;

        double score = candidate.getHitCount() * 0.9D;
        score += Math.max(0, 4 - candidate.getFirstQueryIndex()) * 0.7D;
        if (profile.isStudyIntent()) {
            score += 2.8D;
        }
        if (StringUtils.hasText(item.getTitleHighlight())) {
            score += 2.1D;
        }
        if (StringUtils.hasText(item.getAnswerSummaryHighlight())) {
            score += 1.2D;
        }
        if (StringUtils.hasText(question) && StringUtils.hasText(title) && title.contains(question)) {
            score += 10.5D;
        }
        if (StringUtils.hasText(question) && StringUtils.hasText(answerSummary) && answerSummary.contains(question)) {
            score += 4.5D;
        }

        int matchedTerms = 0;
        for (String term : profile.getTerms()) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            boolean currentMatched = false;
            if (title.contains(term)) {
                score += 5.0D;
                currentMatched = true;
            }
            if (keywords.contains(term)) {
                score += 3.6D;
                currentMatched = true;
            }
            if (answerSummary.contains(term)) {
                score += 2.6D;
                currentMatched = true;
            }
            if (category.contains(term)) {
                score += 1.3D;
                currentMatched = true;
            }
            if (questionCode.contains(term)) {
                score += 1.0D;
                currentMatched = true;
            }
            if (currentMatched) {
                matchedTerms++;
            }
        }
        for (String term : profile.getStrongTerms()) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            if (title.contains(term)) {
                score += 6.5D;
                matchedStrongTerm = true;
                continue;
            }
            if (keywords.contains(term) || answerSummary.contains(term) || category.contains(term) || questionCode.contains(term)) {
                score += 3.2D;
                matchedStrongTerm = true;
            }
        }
        if (!profile.getStrongTerms().isEmpty()
                && !matchedStrongTerm
                && !(StringUtils.hasText(question) && title.contains(question))
                && !(StringUtils.hasText(question) && answerSummary.contains(question))) {
            score -= STRONG_TERM_MISS_PENALTY;
        }
        score += Math.min(matchedTerms, 4) * 0.9D;
        score += Math.min(defaultInt(item.getStudyCount()) / 120.0D, 0.8D);
        return score;
    }

    private boolean isStrongMatchTerm(String term) {
        if (!StringUtils.hasText(term)) {
            return false;
        }
        if (GENERIC_MATCH_TERMS.contains(term)) {
            return false;
        }
        return term.matches(".*\\d.*") || term.length() >= 3;
    }

    private String trimTopicSuffix(String term) {
        if (!StringUtils.hasText(term)) {
            return "";
        }
        String result = term;
        for (String suffix : TOPIC_SUFFIXES) {
            if (result.endsWith(suffix) && result.length() > suffix.length() + 1) {
                result = result.substring(0, result.length() - suffix.length());
                break;
            }
        }
        return result;
    }

    private String normalizeForMatch(String text) {
        String cleaned = cleanText(text);
        if (!StringUtils.hasText(cleaned)) {
            return "";
        }
        return cleaned.replaceAll("\\s+", "").toLowerCase();
    }

    private void mergeArticleHits(Map<Long, RankedArticleHit> target, List<SearchArticleVO> source, int queryIndex) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (SearchArticleVO item : source) {
            if (item == null || item.getId() == null) {
                continue;
            }
            RankedArticleHit hit = target.get(item.getId());
            if (hit == null) {
                target.put(item.getId(), new RankedArticleHit(item, queryIndex));
                continue;
            }
            hit.recordHit(queryIndex, item);
        }
    }

    private void mergeQuestionHits(Map<Long, RankedStudyQuestionHit> target, List<SearchStudyQuestionVO> source, int queryIndex) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (SearchStudyQuestionVO item : source) {
            if (item == null || item.getId() == null) {
                continue;
            }
            RankedStudyQuestionHit hit = target.get(item.getId());
            if (hit == null) {
                target.put(item.getId(), new RankedStudyQuestionHit(item, queryIndex));
                continue;
            }
            hit.recordHit(queryIndex, item);
        }
    }

    private boolean hasAnyKeyword(String text, String... keywords) {
        if (!StringUtils.hasText(text) || keywords == null || keywords.length == 0) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private AiKnowledgeSourceVO buildStudyQuestionSource(StudyQuestion question,
                                                         SearchStudyQuestionVO searchItem,
                                                         StudyUserQuestionProgress progress) {
        AiKnowledgeSourceVO source = new AiKnowledgeSourceVO();
        source.setSourceType("study_question");
        source.setSourceId(question.getId());
        source.setTitle(defaultText(question.getTitle(), searchItem.getTitle()));
        source.setSummary(defaultText(question.getAnswerSummary(), searchItem.getAnswerSummary(), shorten(cleanText(question.getStandardAnswer()), 120)));
        source.setCategoryName(defaultText(searchItem.getCategoryName(), "面试题"));
        source.setRoutePath("/study/learn/" + question.getId());
        source.setPublishTime(formatDateTime(question.getPublishTime()));
        source.setKeywords(defaultText(question.getKeywords(), searchItem.getKeywords(), ""));
        source.setDifficulty(question.getDifficulty());
        source.setDifficultyLabel(formatDifficulty(question.getDifficulty()));
        source.setViewCount(defaultInt(question.getViewCount()));
        source.setStudyCount(defaultInt(question.getStudyCount()));
        if (progress != null) {
            source.setStudyStatus(progress.getStudyStatus());
            source.setStudyStatusLabel(formatStudyStatus(progress.getStudyStatus()));
            source.setIsFavorite(defaultInt(progress.getIsFavorite()));
            source.setIsWrongQuestion(defaultInt(progress.getIsWrongQuestion()));
        }
        return source;
    }

    private AiKnowledgeSourceVO buildArticleSource(Article article, SearchArticleVO searchItem) {
        AiKnowledgeSourceVO source = new AiKnowledgeSourceVO();
        source.setSourceType("article");
        source.setSourceId(article.getId());
        source.setTitle(defaultText(article.getTitle(), searchItem.getTitle()));
        source.setSummary(defaultText(article.getSummary(), searchItem.getSummary(), shorten(extractArticleText(article), 120)));
        source.setCategoryName(defaultText(searchItem.getCategoryName(), article.getCategoryName(), "文章"));
        source.setRoutePath("/article/" + article.getId());
        source.setPublishTime(formatDateTime(article.getPublishTime()));
        source.setViewCount(defaultInt(article.getViewCount()));
        source.setLikeCount(defaultInt(article.getLikeCount()));
        source.setFavoriteCount(defaultInt(article.getFavoriteCount()));
        return source;
    }

    private Map<Long, Article> loadArticleMap(List<SearchArticleVO> articleHits) {
        List<Long> ids = articleHits.stream()
                .map(SearchArticleVO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return articleMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Article::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, StudyQuestion> loadQuestionMap(List<SearchStudyQuestionVO> questionHits) {
        List<Long> ids = questionHits.stream()
                .map(SearchStudyQuestionVO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return studyQuestionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(StudyQuestion::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, StudyUserQuestionProgress> loadProgressMap(Long userId, Set<Long> questionIds) {
        if (userId == null || questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return progressMapper.selectList(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                        .eq(StudyUserQuestionProgress::getUserId, userId)
                        .in(StudyUserQuestionProgress::getQuestionId, questionIds))
                .stream()
                .collect(Collectors.toMap(StudyUserQuestionProgress::getQuestionId, item -> item, (left, right) -> left));
    }

    private <T> List<T> safePageList(PageResult<T> pageResult) {
        return pageResult == null || pageResult.getList() == null ? Collections.emptyList() : pageResult.getList();
    }

    private <T> List<T> limitList(List<T> source, int limit) {
        if (source == null || source.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }
        return source.stream().limit(limit).collect(Collectors.toList());
    }

    private String extractArticleText(Article article) {
        if (article == null) {
            return "";
        }
        String preferred = StringUtils.hasText(article.getContentHtml()) ? article.getContentHtml() : article.getContent();
        return cleanText(preferred);
    }

    private String cleanText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String value = HtmlUtil.cleanHtmlTag(text);
        value = value.replaceAll("\\[(.*?)]\\((.*?)\\)", "$1")
                .replaceAll("`{1,3}", " ")
                .replaceAll("#{1,6}", " ")
                .replaceAll("\\*|>|\\-|\\|", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return value;
    }

    private String shorten(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "无";
        }
        String value = text.trim();
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String defaultText(String... values) {
        if (values == null) {
            return "无";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "无";
    }

    private String formatDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return "未知";
        }
        switch (difficulty) {
            case 1:
                return "简单";
            case 2:
                return "中等";
            case 3:
                return "偏难";
            case 4:
                return "困难";
            default:
                return String.valueOf(difficulty);
        }
    }

    private String formatStudyStatus(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "未开始";
            case 1:
                return "学习中";
            case 2:
                return "已掌握";
            case 3:
                return "待复习";
            default:
                return String.valueOf(status);
        }
    }

    private String formatDateTime(LocalDateTime time) {
        return time == null ? "暂无" : time.format(DATE_TIME_FORMATTER);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static class SearchBundle {

        private final List<SearchArticleVO> articleHits;
        private final List<SearchStudyQuestionVO> questionHits;
        private final String summary;

        private SearchBundle(List<SearchArticleVO> articleHits, List<SearchStudyQuestionVO> questionHits, String summary) {
            this.articleHits = articleHits;
            this.questionHits = questionHits;
            this.summary = summary;
        }

        public List<SearchArticleVO> getArticleHits() {
            return articleHits;
        }

        public List<SearchStudyQuestionVO> getQuestionHits() {
            return questionHits;
        }

        public String getSummary() {
            return summary;
        }
    }

    private static class QueryProfile {

        private final String question;
        private final List<String> queries;
        private final List<String> terms;
        private final List<String> strongTerms;
        private final boolean articleIntent;
        private final boolean studyIntent;
        private final boolean projectKnowledgeIntent;

        private QueryProfile(String question,
                             List<String> queries,
                             List<String> terms,
                             List<String> strongTerms,
                             boolean articleIntent,
                             boolean studyIntent,
                             boolean projectKnowledgeIntent) {
            this.question = question;
            this.queries = queries;
            this.terms = terms;
            this.strongTerms = strongTerms;
            this.articleIntent = articleIntent;
            this.studyIntent = studyIntent;
            this.projectKnowledgeIntent = projectKnowledgeIntent;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getQueries() {
            return queries;
        }

        public List<String> getTerms() {
            return terms;
        }

        public List<String> getStrongTerms() {
            return strongTerms;
        }

        public boolean isArticleIntent() {
            return articleIntent;
        }

        public boolean isStudyIntent() {
            return studyIntent;
        }

        public boolean isProjectKnowledgeIntent() {
            return projectKnowledgeIntent;
        }
    }

    private static class RankedArticleHit {

        private SearchArticleVO searchItem;
        private int firstQueryIndex;
        private int hitCount;

        private RankedArticleHit(SearchArticleVO searchItem, int firstQueryIndex) {
            this.searchItem = searchItem;
            this.firstQueryIndex = firstQueryIndex;
            this.hitCount = 1;
        }

        private void recordHit(int queryIndex, SearchArticleVO latestItem) {
            this.hitCount++;
            this.firstQueryIndex = Math.min(this.firstQueryIndex, queryIndex);
            if (!StringUtils.hasText(this.searchItem.getTitleHighlight()) && StringUtils.hasText(latestItem.getTitleHighlight())) {
                this.searchItem.setTitleHighlight(latestItem.getTitleHighlight());
            }
            if (!StringUtils.hasText(this.searchItem.getSummaryHighlight()) && StringUtils.hasText(latestItem.getSummaryHighlight())) {
                this.searchItem.setSummaryHighlight(latestItem.getSummaryHighlight());
            }
        }

        public SearchArticleVO getSearchItem() {
            return searchItem;
        }

        public int getFirstQueryIndex() {
            return firstQueryIndex;
        }

        public int getHitCount() {
            return hitCount;
        }
    }

    private static class RankedStudyQuestionHit {

        private SearchStudyQuestionVO searchItem;
        private int firstQueryIndex;
        private int hitCount;

        private RankedStudyQuestionHit(SearchStudyQuestionVO searchItem, int firstQueryIndex) {
            this.searchItem = searchItem;
            this.firstQueryIndex = firstQueryIndex;
            this.hitCount = 1;
        }

        private void recordHit(int queryIndex, SearchStudyQuestionVO latestItem) {
            this.hitCount++;
            this.firstQueryIndex = Math.min(this.firstQueryIndex, queryIndex);
            if (!StringUtils.hasText(this.searchItem.getTitleHighlight()) && StringUtils.hasText(latestItem.getTitleHighlight())) {
                this.searchItem.setTitleHighlight(latestItem.getTitleHighlight());
            }
            if (!StringUtils.hasText(this.searchItem.getAnswerSummaryHighlight()) && StringUtils.hasText(latestItem.getAnswerSummaryHighlight())) {
                this.searchItem.setAnswerSummaryHighlight(latestItem.getAnswerSummaryHighlight());
            }
        }

        public SearchStudyQuestionVO getSearchItem() {
            return searchItem;
        }

        public int getFirstQueryIndex() {
            return firstQueryIndex;
        }

        public int getHitCount() {
            return hitCount;
        }
    }

    private static class ScoredArticleHit {

        private final SearchArticleVO searchItem;
        private final double score;
        private final int strongTermMatchCount;
        private final boolean exactQuestionMatched;

        private ScoredArticleHit(SearchArticleVO searchItem,
                                 double score,
                                 int strongTermMatchCount,
                                 boolean exactQuestionMatched) {
            this.searchItem = searchItem;
            this.score = score;
            this.strongTermMatchCount = strongTermMatchCount;
            this.exactQuestionMatched = exactQuestionMatched;
        }

        public SearchArticleVO getSearchItem() {
            return searchItem;
        }

        public double getScore() {
            return score;
        }

        public int getStrongTermMatchCount() {
            return strongTermMatchCount;
        }

        public boolean isExactQuestionMatched() {
            return exactQuestionMatched;
        }
    }

    private static class ScoredStudyQuestionHit {

        private final SearchStudyQuestionVO searchItem;
        private final double score;
        private final int strongTermMatchCount;
        private final boolean exactQuestionMatched;

        private ScoredStudyQuestionHit(SearchStudyQuestionVO searchItem,
                                       double score,
                                       int strongTermMatchCount,
                                       boolean exactQuestionMatched) {
            this.searchItem = searchItem;
            this.score = score;
            this.strongTermMatchCount = strongTermMatchCount;
            this.exactQuestionMatched = exactQuestionMatched;
        }

        public SearchStudyQuestionVO getSearchItem() {
            return searchItem;
        }

        public double getScore() {
            return score;
        }

        public int getStrongTermMatchCount() {
            return strongTermMatchCount;
        }

        public boolean isExactQuestionMatched() {
            return exactQuestionMatched;
        }
    }
}
