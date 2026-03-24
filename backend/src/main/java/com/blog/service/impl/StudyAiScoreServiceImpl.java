package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.constant.StudyConstants;
import com.blog.common.exception.BusinessException;
import com.blog.dto.request.AdminAiScoreTestRequest;
import com.blog.dto.response.AdminAiScoreTestVO;
import com.blog.dto.response.StudyAiScoreVO;
import com.blog.entity.AiConfig;
import com.blog.entity.StudyAiScoreRecord;
import com.blog.entity.StudyAnswerRecord;
import com.blog.entity.StudyQuestion;
import com.blog.mapper.StudyAiScoreRecordMapper;
import com.blog.service.AiChatService;
import com.blog.service.StudyAiScoreService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class StudyAiScoreServiceImpl extends ServiceImpl<StudyAiScoreRecordMapper, StudyAiScoreRecord> implements StudyAiScoreService {

    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public StudyAiScoreServiceImpl(AiChatService aiChatService, ObjectMapper objectMapper) {
        this.aiChatService = aiChatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public StudyAiScoreRecord scoreAnswer(StudyQuestion question, StudyAnswerRecord answerRecord, Long taskId, Long taskItemId, Long userId) {
        StudyAiScoreRecord record = new StudyAiScoreRecord();
        record.setScoreNo(buildNo("AIS"));
        record.setAnswerId(answerRecord.getId());
        record.setTaskId(taskId);
        record.setTaskItemId(taskItemId);
        record.setQuestionId(question.getId());
        record.setUserId(userId);
        record.setPromptVersion(StringUtils.hasText(question.getAiScorePromptVersion()) ? question.getAiScorePromptVersion() : "v1");
        record.setFullScore(question.getScoreFullMark() == null ? new BigDecimal("100") : question.getScoreFullMark());
        record.setScoreStatus(StudyConstants.AI_SCORE_STATUS_RUNNING);
        record.setRequestTime(LocalDateTime.now());
        save(record);

        AiConfig config = aiChatService.getStudyScoreConfig();
        if (config == null || config.getEnabled() == null || config.getEnabled() != 1 || !StringUtils.hasText(config.getApiKey())) {
            record.setScoreStatus(StudyConstants.AI_SCORE_STATUS_FAILED);
            record.setErrorMessage("未找到可用的AI评分配置");
            record.setResponseTime(LocalDateTime.now());
            updateById(record);
            return record;
        }

        record.setAiConfigId(config.getId());
        record.setModelProvider(config.getProvider());
        record.setModelName(config.getModel());

        long start = System.currentTimeMillis();
        try {
            String rawResponse = callAi(config, question, answerRecord);
            record.setRawResponse(rawResponse);
            parseScoreResponse(record, rawResponse);
            record.setScoreStatus(StudyConstants.AI_SCORE_STATUS_SUCCESS);
            record.setResponseTime(LocalDateTime.now());
            record.setDurationMs((int) (System.currentTimeMillis() - start));
            updateById(record);
        } catch (Exception e) {
            log.error("AI评分失败, answerId={}", answerRecord.getId(), e);
            record.setScoreStatus(StudyConstants.AI_SCORE_STATUS_FAILED);
            record.setErrorMessage(e.getMessage());
            record.setResponseTime(LocalDateTime.now());
            record.setDurationMs((int) (System.currentTimeMillis() - start));
            updateById(record);
        }
        return getById(record.getId());
    }

    @Override
    public StudyAiScoreVO getScoreDetail(Long scoreRecordId) {
        if (scoreRecordId == null) {
            return null;
        }
        StudyAiScoreRecord record = getById(scoreRecordId);
        if (record == null) {
            return null;
        }
        StudyAiScoreVO vo = new StudyAiScoreVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    @Override
    public AdminAiScoreTestVO testScore(AdminAiScoreTestRequest request) {
        validatePreviewRequest(request);
        AiConfig config = aiChatService.getConfigById(request.getConfigId());
        if (config == null) {
            throw new BusinessException("测试模型不存在");
        }
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException("请先启用该模型后再进行评分测试");
        }
        if (config.getUseForStudyScore() == null || config.getUseForStudyScore() != 1) {
            throw new BusinessException("该模型未启用面试评分场景");
        }
        if (!StringUtils.hasText(config.getApiKey())) {
            throw new BusinessException("请先为该模型配置 API Key");
        }
        if (!StringUtils.hasText(config.getBaseUrl())) {
            throw new BusinessException("请先为该模型配置 API 地址");
        }

        StudyQuestion question = buildPreviewQuestion(request);
        StudyAnswerRecord answerRecord = buildPreviewAnswer(request);
        StudyAiScoreRecord previewRecord = new StudyAiScoreRecord();
        previewRecord.setPromptVersion(StringUtils.hasText(request.getPromptOverride()) ? "preview-override" : "preview");
        previewRecord.setFullScore(question.getScoreFullMark());
        previewRecord.setModelProvider(config.getProvider());
        previewRecord.setModelName(config.getModel());
        previewRecord.setAiConfigId(config.getId());
        previewRecord.setRequestTime(LocalDateTime.now());
        previewRecord.setScoreStatus(StudyConstants.AI_SCORE_STATUS_RUNNING);

        long start = System.currentTimeMillis();
        try {
            String rawResponse = callAi(config, question, answerRecord, request.getPromptOverride());
            previewRecord.setRawResponse(rawResponse);
            parseScoreResponse(previewRecord, rawResponse);
            previewRecord.setScoreStatus(StudyConstants.AI_SCORE_STATUS_SUCCESS);
            previewRecord.setResponseTime(LocalDateTime.now());
            previewRecord.setDurationMs((int) (System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.error("管理端评分测试失败, configId={}", request.getConfigId(), e);
            previewRecord.setScoreStatus(StudyConstants.AI_SCORE_STATUS_FAILED);
            previewRecord.setErrorMessage(e.getMessage());
            previewRecord.setResponseTime(LocalDateTime.now());
            previewRecord.setDurationMs((int) (System.currentTimeMillis() - start));
        }
        return buildPreviewResponse(config, question, request.getPromptOverride(), previewRecord);
    }

    private String callAi(AiConfig config, StudyQuestion question, StudyAnswerRecord answerRecord) throws Exception {
        return callAi(config, question, answerRecord, null);
    }

    private String callAi(AiConfig config, StudyQuestion question, StudyAnswerRecord answerRecord, String promptOverride) throws Exception {
        String baseUrl = StringUtils.hasText(config.getBaseUrl()) ? config.getBaseUrl() : "https://api.deepseek.com/v1";
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", buildSystemPrompt(config, promptOverride));
        messages.add(system);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", buildUserPrompt(question, answerRecord));
        messages.add(user);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("max_tokens", config.getMaxTokens() == null ? 1200 : config.getMaxTokens());
        requestBody.put("temperature", config.getTemperature() == null ? 0.2 : config.getTemperature());

        String json = objectMapper.writeValueAsString(requestBody);
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("AI评分接口调用失败: HTTP " + response.code());
            }
            String body = response.body() == null ? "" : response.body().string();
            JsonNode root = objectMapper.readTree(body);
            JsonNode usage = root.path("usage");
            JsonNode choices = root.path("choices");
            if (!usage.isMissingNode()) {
                // usage handled in parseScoreResponse from raw payload
            }
            if (!choices.isArray() || choices.isEmpty()) {
                throw new RuntimeException("AI评分接口未返回有效内容");
            }
            JsonNode contentNode = choices.get(0).path("message").path("content");
            if (contentNode.isMissingNode() || !StringUtils.hasText(contentNode.asText())) {
                throw new RuntimeException("AI评分接口未返回评分文本");
            }
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("apiResponse", root);
            wrapper.put("scoreContent", contentNode.asText());
            return objectMapper.writeValueAsString(wrapper);
        }
    }

    private void parseScoreResponse(StudyAiScoreRecord record, String rawResponse) throws Exception {
        JsonNode wrapper = objectMapper.readTree(rawResponse);
        JsonNode apiResponse = wrapper.path("apiResponse");
        JsonNode usage = apiResponse.path("usage");
        if (!usage.isMissingNode()) {
            record.setRequestTokens(usage.path("prompt_tokens").asInt(0));
            record.setResponseTokens(usage.path("completion_tokens").asInt(0));
            record.setTotalTokens(usage.path("total_tokens").asInt(0));
        }

        String content = wrapper.path("scoreContent").asText();
        String normalized = stripCodeFence(content);
        JsonNode scoreNode;
        try {
            scoreNode = objectMapper.readTree(normalized);
        } catch (Exception ex) {
            int start = normalized.indexOf('{');
            int end = normalized.lastIndexOf('}');
            if (start >= 0 && end > start) {
                scoreNode = objectMapper.readTree(normalized.substring(start, end + 1));
            } else {
                throw new RuntimeException("AI评分结果不是有效JSON");
            }
        }

        BigDecimal aiScore = new BigDecimal(scoreNode.path("score").asText("0")).setScale(2, RoundingMode.HALF_UP);
        record.setAiScore(aiScore.max(BigDecimal.ZERO).min(record.getFullScore()));
        record.setDimensionScores(toJsonText(scoreNode.path("dimensionScores")));
        record.setKeywordHitJson(toJsonText(scoreNode.path("keywordHit")));
        record.setStrengthsText(readText(scoreNode, "strengths", "strengthsText"));
        record.setWeaknessesText(readText(scoreNode, "weaknesses", "weaknessesText"));
        record.setSuggestionText(readText(scoreNode, "suggestions", "suggestionText"));
        record.setResultLevel(resolveResultLevel(record.getAiScore()));
    }

    private String buildSystemPrompt(AiConfig config, String promptOverride) {
        String basePrompt = StringUtils.hasText(promptOverride)
                ? promptOverride.trim()
                : StringUtils.hasText(config.getScoreSystemPrompt())
                ? config.getScoreSystemPrompt().trim()
                : "你是一名严格但客观的Java面试官，请根据题目、标准答案和候选人答案进行评分。";
        return basePrompt
                + " 必须返回JSON，不要输出任何额外解释。"
                + " JSON字段固定为：score, dimensionScores, keywordHit, strengths, weaknesses, suggestions。";
    }

    private StudyQuestion buildPreviewQuestion(AdminAiScoreTestRequest request) {
        StudyQuestion question = new StudyQuestion();
        question.setId(0L);
        question.setTitle(request.getQuestionTitle().trim());
        question.setQuestionStem(StringUtils.hasText(request.getQuestionStem()) ? request.getQuestionStem().trim() : request.getQuestionTitle().trim());
        question.setStandardAnswer(request.getStandardAnswer().trim());
        question.setScoreFullMark(defaultPreviewScore(request.getScoreFullMark(), new BigDecimal("100")));
        question.setScorePassMark(defaultPreviewScore(request.getScorePassMark(), new BigDecimal("60")));
        question.setScoreRubricJson(StringUtils.hasText(request.getScoreRubricJson()) ? request.getScoreRubricJson().trim() : null);
        question.setAiScorePromptVersion("preview");
        return question;
    }

    private StudyAnswerRecord buildPreviewAnswer(AdminAiScoreTestRequest request) {
        StudyAnswerRecord answerRecord = new StudyAnswerRecord();
        answerRecord.setId(0L);
        answerRecord.setAnswerContent(request.getCandidateAnswer().trim());
        answerRecord.setSelfAssessmentResult(request.getCandidateSelfAssessment() == null ? StudyConstants.SELF_RESULT_REMEMBER : request.getCandidateSelfAssessment());
        answerRecord.setSelfScore(request.getCandidateSelfScore() == null
                ? null
                : request.getCandidateSelfScore().setScale(2, RoundingMode.HALF_UP));
        return answerRecord;
    }

    private AdminAiScoreTestVO buildPreviewResponse(AiConfig config, StudyQuestion question, String promptOverride, StudyAiScoreRecord record) {
        AdminAiScoreTestVO vo = new AdminAiScoreTestVO();
        vo.setConfigId(config.getId());
        vo.setConfigDisplayName(StringUtils.hasText(config.getDisplayName()) ? config.getDisplayName() : config.getModel());
        vo.setProvider(config.getProvider());
        vo.setModelName(config.getModel());
        vo.setPromptVersion(record.getPromptVersion());
        vo.setResolvedSystemPrompt(buildSystemPrompt(config, promptOverride));
        vo.setQuestionTitle(question.getTitle());
        vo.setFullScore(question.getScoreFullMark());
        vo.setPassScore(question.getScorePassMark());
        vo.setAiScore(record.getAiScore());
        vo.setPassed(resolvePassed(record.getAiScore(), question.getScorePassMark()));
        vo.setResultLevel(record.getResultLevel());
        vo.setResultLevelLabel(resolveResultLevelLabel(record.getResultLevel()));
        vo.setDimensionScores(record.getDimensionScores());
        vo.setKeywordHitJson(record.getKeywordHitJson());
        vo.setStrengthsText(record.getStrengthsText());
        vo.setWeaknessesText(record.getWeaknessesText());
        vo.setSuggestionText(record.getSuggestionText());
        vo.setRequestTokens(record.getRequestTokens());
        vo.setResponseTokens(record.getResponseTokens());
        vo.setTotalTokens(record.getTotalTokens());
        vo.setScoreStatus(record.getScoreStatus());
        vo.setErrorMessage(record.getErrorMessage());
        vo.setDurationMs(record.getDurationMs());
        vo.setRawResponse(record.getRawResponse());
        return vo;
    }

    private BigDecimal defaultPreviewScore(BigDecimal value, BigDecimal defaultValue) {
        return (value == null ? defaultValue : value).setScale(2, RoundingMode.HALF_UP);
    }

    private void validatePreviewRequest(AdminAiScoreTestRequest request) {
        BigDecimal fullScore = defaultPreviewScore(request.getScoreFullMark(), new BigDecimal("100"));
        BigDecimal passScore = defaultPreviewScore(request.getScorePassMark(), new BigDecimal("60"));
        if (passScore.compareTo(fullScore) > 0) {
            throw new BusinessException("及格分不能大于满分");
        }
        if (request.getCandidateSelfScore() != null && request.getCandidateSelfScore().compareTo(fullScore) > 0) {
            throw new BusinessException("自评分不能大于满分");
        }
        if (StringUtils.hasText(request.getScoreRubricJson())) {
            try {
                objectMapper.readTree(request.getScoreRubricJson().trim());
            } catch (Exception e) {
                throw new BusinessException("评分规则 JSON 格式不正确");
            }
        }
    }

    private String buildUserPrompt(StudyQuestion question, StudyAnswerRecord answerRecord) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", question.getTitle());
        payload.put("questionStem", question.getQuestionStem());
        payload.put("standardAnswer", question.getStandardAnswer());
        payload.put("scoreFullMark", question.getScoreFullMark());
        payload.put("scorePassMark", question.getScorePassMark());
        payload.put("rubric", question.getScoreRubricJson());
        payload.put("candidateAnswer", answerRecord.getAnswerContent());
        payload.put("candidateSelfAssessment", answerRecord.getSelfAssessmentResult());
        payload.put("candidateSelfScore", answerRecord.getSelfScore());
        payload.put("requirements", List.of(
                "score 为 0 到满分之间的数字",
                "dimensionScores 为对象，建议包含 accuracy、completeness、clarity、practicality",
                "keywordHit 为对象或数组，指出命中的关键点",
                "strengths / weaknesses / suggestions 使用中文"
        ));
        return objectMapper.writeValueAsString(payload);
    }

    private String stripCodeFence(String text) {
        String value = text.trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```[a-zA-Z]*", "").trim();
        }
        if (value.endsWith("```")) {
            value = value.substring(0, value.length() - 3).trim();
        }
        return value;
    }

    private String toJsonText(JsonNode node) throws Exception {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return objectMapper.writeValueAsString(node);
    }

    private String readText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.path(key);
            if (!value.isMissingNode() && !value.isNull() && StringUtils.hasText(value.asText())) {
                return value.asText();
            }
        }
        return null;
    }

    private Integer resolveResultLevel(BigDecimal score) {
        if (score == null) {
            return null;
        }
        if (score.compareTo(new BigDecimal("90")) >= 0) {
            return StudyConstants.AI_RESULT_EXCELLENT;
        }
        if (score.compareTo(new BigDecimal("75")) >= 0) {
            return StudyConstants.AI_RESULT_GOOD;
        }
        if (score.compareTo(new BigDecimal("60")) >= 0) {
            return StudyConstants.AI_RESULT_PASS;
        }
        return StudyConstants.AI_RESULT_WEAK;
    }

    private Boolean resolvePassed(BigDecimal score, BigDecimal passScore) {
        if (score == null || passScore == null) {
            return null;
        }
        return score.compareTo(passScore) >= 0;
    }

    private String resolveResultLevelLabel(Integer resultLevel) {
        if (resultLevel == null) {
            return null;
        }
        if (StudyConstants.AI_RESULT_EXCELLENT.equals(resultLevel)) {
            return "优秀";
        }
        if (StudyConstants.AI_RESULT_GOOD.equals(resultLevel)) {
            return "良好";
        }
        if (StudyConstants.AI_RESULT_PASS.equals(resultLevel)) {
            return "及格";
        }
        if (StudyConstants.AI_RESULT_WEAK.equals(resultLevel)) {
            return "待加强";
        }
        return "未知";
    }

    private String buildNo(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }
}
