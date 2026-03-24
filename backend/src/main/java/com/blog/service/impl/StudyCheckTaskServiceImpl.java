package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.constant.StudyConstants;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.PageResult;
import com.blog.config.StudyCacheProperties;
import com.blog.config.StudyCheckRetentionProperties;
import com.blog.dto.request.StudyAnswerSubmitRequest;
import com.blog.dto.request.StudyTaskCreateRequest;
import com.blog.dto.response.StudyAiScoreVO;
import com.blog.dto.response.StudyCheckHistoryVO;
import com.blog.dto.response.StudyCheckStatisticsVO;
import com.blog.dto.response.StudyCheckTaskItemVO;
import com.blog.dto.response.StudyCheckTaskVO;
import com.blog.entity.StudyAiScoreRecord;
import com.blog.entity.StudyAnswerRecord;
import com.blog.entity.StudyCategory;
import com.blog.entity.StudyCheckTask;
import com.blog.entity.StudyCheckTaskItem;
import com.blog.entity.StudyQuestion;
import com.blog.entity.StudyUserQuestionProgress;
import com.blog.entity.User;
import com.blog.mapper.StudyAiScoreRecordMapper;
import com.blog.mapper.StudyAnswerRecordMapper;
import com.blog.mapper.StudyCategoryMapper;
import com.blog.mapper.StudyCheckTaskItemMapper;
import com.blog.mapper.StudyCheckTaskMapper;
import com.blog.mapper.StudyQuestionMapper;
import com.blog.mapper.StudyUserQuestionProgressMapper;
import com.blog.service.RedisService;
import com.blog.service.StudyAiScoreService;
import com.blog.service.StudyCheckTaskService;
import com.blog.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudyCheckTaskServiceImpl extends ServiceImpl<StudyCheckTaskMapper, StudyCheckTask> implements StudyCheckTaskService {

    private final StudyCheckTaskItemMapper taskItemMapper;
    private final StudyQuestionMapper questionMapper;
    private final StudyCategoryMapper categoryMapper;
    private final StudyUserQuestionProgressMapper progressMapper;
    private final StudyAnswerRecordMapper answerRecordMapper;
    private final StudyAiScoreRecordMapper aiScoreRecordMapper;
    private final StudyAiScoreService studyAiScoreService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final StudyCacheProperties studyCacheProperties;
    private final StudyCheckRetentionProperties studyCheckRetentionProperties;
    private final TransactionTemplate transactionTemplate;

    public StudyCheckTaskServiceImpl(StudyCheckTaskItemMapper taskItemMapper,
                                     StudyQuestionMapper questionMapper,
                                     StudyCategoryMapper categoryMapper,
                                     StudyUserQuestionProgressMapper progressMapper,
                                     StudyAnswerRecordMapper answerRecordMapper,
                                     StudyAiScoreRecordMapper aiScoreRecordMapper,
                                     StudyAiScoreService studyAiScoreService,
                                     UserService userService,
                                     ObjectMapper objectMapper,
                                     RedisService redisService,
                                     StudyCacheProperties studyCacheProperties,
                                     StudyCheckRetentionProperties studyCheckRetentionProperties,
                                     TransactionTemplate transactionTemplate) {
        this.taskItemMapper = taskItemMapper;
        this.questionMapper = questionMapper;
        this.categoryMapper = categoryMapper;
        this.progressMapper = progressMapper;
        this.answerRecordMapper = answerRecordMapper;
        this.aiScoreRecordMapper = aiScoreRecordMapper;
        this.studyAiScoreService = studyAiScoreService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.redisService = redisService;
        this.studyCacheProperties = studyCacheProperties;
        this.studyCheckRetentionProperties = studyCheckRetentionProperties;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public StudyCheckTaskVO createTask(Long userId, StudyTaskCreateRequest request) {
        validateCreateRequest(request);
        Integer scoringMode = normalizeScoringMode(request.getScoringMode());
        Integer needAiScore = resolveNeedAiScore(scoringMode);
        Integer allowSelfAssessment = resolveAllowSelfAssessment(scoringMode);
        List<StudyQuestion> candidates = loadCandidateQuestions(userId, request, scoringMode);
        if (candidates.isEmpty()) {
            if (StudyConstants.SCORING_MODE_AI_ONLY.equals(scoringMode)) {
                throw new BusinessException("当前筛选下没有支持 AI 评分的题目，请调整题库范围或切换评分模式");
            }
            throw new BusinessException("没有匹配的题目可用于抽查");
        }
        Collections.shuffle(candidates);
        List<StudyQuestion> selected = candidates.stream().limit(request.getQuestionCount()).collect(Collectors.toList());
        if (selected.isEmpty()) {
            throw new BusinessException("题目数量不足，无法创建任务");
        }

        StudyCheckTask task = new StudyCheckTask();
        task.setTaskNo(buildNo("TASK"));
        task.setUserId(userId);
        task.setTaskName(request.getTaskName());
        task.setTaskSource(StudyConstants.TASK_SOURCE_MANUAL);
        task.setCheckMode(request.getCheckMode());
        task.setCategoryId(request.getCategoryId());
        task.setFilterJson(buildFilterJson(request, scoringMode, needAiScore, allowSelfAssessment));
        task.setQuestionCount(selected.size());
        task.setAnsweredCount(0);
        task.setScoredCount(0);
        task.setNeedAiScore(needAiScore);
        task.setAllowSelfAssessment(allowSelfAssessment);
        task.setShowStandardAnswer(request.getShowStandardAnswer() == null ? 1 : request.getShowStandardAnswer());
        task.setScoringMode(scoringMode);
        task.setScoreRuleJson(selected.get(0).getScoreRubricJson());
        task.setFullScore(selected.stream().map(item -> defaultScore(item.getScoreFullMark())).reduce(BigDecimal.ZERO, BigDecimal::add));
        task.setSelfScore(BigDecimal.ZERO);
        task.setAiScore(BigDecimal.ZERO);
        task.setFinalScore(BigDecimal.ZERO);
        task.setRememberCount(0);
        task.setFuzzyCount(0);
        task.setForgetCount(0);
        task.setExcellentCount(0);
        task.setPassCount(0);
        task.setFailedCount(0);
        task.setStatus(StudyConstants.TASK_STATUS_RUNNING);
        task.setStartTime(LocalDateTime.now());
        task.setDurationSeconds(0);
        save(task);

        int index = 1;
        for (StudyQuestion question : selected) {
            StudyCheckTaskItem item = new StudyCheckTaskItem();
            item.setTaskId(task.getId());
            item.setQuestionId(question.getId());
            item.setQuestionVersionNo(question.getVersionNo());
            item.setSortOrder(index++);
            item.setQuestionTitleSnapshot(question.getTitle());
            item.setQuestionStemSnapshot(question.getQuestionStem());
            item.setStandardAnswerSnapshot(question.getStandardAnswer());
            item.setScoreFullMark(defaultScore(question.getScoreFullMark()));
            item.setScorePassMark(defaultPassScore(question.getScorePassMark()));
            item.setStatus(StudyConstants.TASK_ITEM_STATUS_PENDING);
            item.setAnswerCount(0);
            item.setViewAnswerFlag(0);
            item.setAnswerDurationSeconds(0);
            taskItemMapper.insert(item);
        }
        clearStudyUserCache(userId);
        return getTaskDetail(userId, task.getId(), false);
    }

    @Override
    public StudyCheckTaskVO getTaskDetail(Long userId, Long taskId, boolean adminView) {
        StudyCheckTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("抽查任务不存在");
        }
        if (!adminView && !task.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该任务");
        }
        List<StudyCheckTaskItem> items = taskItemMapper.selectList(new LambdaQueryWrapper<StudyCheckTaskItem>()
                .eq(StudyCheckTaskItem::getTaskId, taskId)
                .orderByAsc(StudyCheckTaskItem::getSortOrder));
        Map<Long, StudyAiScoreVO> scoreMap = loadAiScoreMap(items);
        Map<Long, StudyAnswerRecord> latestAnswerMap = loadLatestAnswerMap(items);
        StudyCheckTaskVO vo = new StudyCheckTaskVO();
        BeanUtils.copyProperties(task, vo);
        StudyCategory category = task.getCategoryId() == null ? null : categoryMapper.selectById(task.getCategoryId());
        vo.setCategoryName(category != null ? category.getCategoryName() : null);
        vo.setItems(items.stream().map(item -> toTaskItemVO(item, scoreMap.get(item.getLastAiScoreRecordId()), latestAnswerMap.get(item.getId()))).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public void markViewAnswer(Long userId, Long taskId, Long taskItemId) {
        StudyCheckTaskItem item = getOwnedTaskItem(userId, taskId, taskItemId);
        item.setViewAnswerFlag(1);
        item.setViewAnswerTime(LocalDateTime.now());
        taskItemMapper.updateById(item);
    }

    @Override
    public StudyCheckTaskItemVO submitAnswer(Long userId, Long taskId, Long taskItemId, StudyAnswerSubmitRequest request) {
        StudyCheckTask task = getOwnedTask(userId, taskId);
        validateTaskCanSubmit(task);

        AnswerSubmitContext context = transactionTemplate.execute(status -> prepareAnswerSubmission(userId, task, taskItemId, request));
        if (context == null) {
            throw new BusinessException("提交失败，请稍后重试");
        }
        if (context.getExistingResult() != null) {
            return context.getExistingResult();
        }
        if (context.isScoringInProgress()) {
            throw new BusinessException("本题正在处理中，请勿重复提交");
        }

        StudyAiScoreRecord aiScoreRecord = null;
        BigDecimal aiScore = null;
        if (context.isNeedAi()) {
            aiScoreRecord = studyAiScoreService.scoreAnswer(context.getQuestion(), context.getAnswerRecord(), taskId, taskItemId, userId);
            if (aiScoreRecord != null) {
                aiScore = aiScoreRecord.getAiScore();
                context.getAnswerRecord().setAiScoreStatus(aiScoreRecord.getScoreStatus());
            } else {
                context.getAnswerRecord().setAiScoreStatus(StudyConstants.AI_SCORE_STATUS_FAILED);
            }
        }

        final StudyAiScoreRecord finalAiScoreRecord = aiScoreRecord;
        final BigDecimal finalAiScore = aiScore;
        StudyCheckTaskItemVO result = transactionTemplate.execute(status ->
                finalizeAnswerSubmission(userId, task, context, finalAiScoreRecord, finalAiScore));
        if (result == null) {
            throw new BusinessException("提交失败，请稍后重试");
        }
        return result;
    }

    @Override
    @Transactional
    public StudyCheckTaskVO finishTask(Long userId, Long taskId) {
        StudyCheckTask task = getOwnedTask(userId, taskId);
        syncTaskProgress(task);
        task = getById(taskId);
        task.setStatus(StudyConstants.TASK_STATUS_COMPLETED);
        task.setSubmitTime(LocalDateTime.now());
        if (task.getStartTime() != null) {
            task.setDurationSeconds((int) java.time.Duration.between(task.getStartTime(), task.getSubmitTime()).getSeconds());
        }
        updateById(task);
        clearStudyUserCache(userId);
        return getTaskDetail(userId, taskId, false);
    }

    @Override
    public PageResult<StudyCheckHistoryVO> getHistory(Long userId, Integer page, Integer pageSize, Integer status) {
        LocalDateTime cutoff = resolveHistoryCutoff();
        QueryWrapper<StudyCheckTask> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.and(q -> q.ge("submit_time", cutoff).or().isNull("submit_time").ge("create_time", cutoff));
        wrapper.orderByDesc("create_time");
        Page<StudyCheckTask> pageResult = page(new Page<>(page, pageSize), wrapper);
        return PageResult.of(buildHistoryVO(pageResult.getRecords()), pageResult.getTotal(), page, pageSize);
    }

    @Override
    public StudyCheckStatisticsVO getStatistics(Long userId) {
        if (userId != null) {
            StudyCheckStatisticsVO cached = redisService.get(redisService.buildStudyCheckStatsCacheKey(userId), StudyCheckStatisticsVO.class);
            if (cached != null) {
                return cached;
            }
        }
        StudyCheckStatisticsVO vo = new StudyCheckStatisticsVO();
        vo.setTotalTaskCount(count(new LambdaQueryWrapper<StudyCheckTask>().eq(StudyCheckTask::getUserId, userId)));
        vo.setCompletedTaskCount(count(new LambdaQueryWrapper<StudyCheckTask>()
                .eq(StudyCheckTask::getUserId, userId)
                .eq(StudyCheckTask::getStatus, StudyConstants.TASK_STATUS_COMPLETED)));
        vo.setTotalQuestionCount(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>().eq(StudyUserQuestionProgress::getUserId, userId)));
        vo.setAnsweredQuestionCount(answerRecordMapper.selectCount(new LambdaQueryWrapper<StudyAnswerRecord>().eq(StudyAnswerRecord::getUserId, userId)));
        vo.setFavoriteQuestionCount(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getIsFavorite, 1)));
        vo.setWrongQuestionCount(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getIsWrongQuestion, 1)));
        vo.setMasteredQuestionCount(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getStudyStatus, StudyConstants.STUDY_STATUS_MASTERED)));
        vo.setReviewingQuestionCount(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getStudyStatus, StudyConstants.STUDY_STATUS_REVIEW)));

        QueryWrapper<StudyCheckTask> avgWrapper = new QueryWrapper<>();
        avgWrapper.select("AVG(final_score) avgScore", "MAX(final_score) bestScore")
                .eq("user_id", userId)
                .eq("status", StudyConstants.TASK_STATUS_COMPLETED);
        List<Map<String, Object>> rows = baseMapper.selectMaps(avgWrapper);
        Map<String, Object> row = firstValidRow(rows);
        if (row != null) {
            vo.setAvgScore(readDecimal(row.get("avgScore")));
            vo.setBestScore(readDecimal(row.get("bestScore")));
        } else {
            vo.setAvgScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            vo.setBestScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        StudyCheckTask latest = getOne(new LambdaQueryWrapper<StudyCheckTask>()
                .eq(StudyCheckTask::getUserId, userId)
                .eq(StudyCheckTask::getStatus, StudyConstants.TASK_STATUS_COMPLETED)
                .orderByDesc(StudyCheckTask::getSubmitTime)
                .last("LIMIT 1"));
        vo.setLatestScore(latest != null ? latest.getFinalScore() : BigDecimal.ZERO);
        if (userId != null) {
            redisService.setWithMinutes(redisService.buildStudyCheckStatsCacheKey(userId), vo, studyCacheProperties.getCheckStatsMinutes());
        }
        return vo;
    }

    @Override
    public PageResult<StudyCheckHistoryVO> getAdminTaskPage(Integer page, Integer pageSize, Long userId, Long categoryId, Integer status) {
        LambdaQueryWrapper<StudyCheckTask> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(StudyCheckTask::getUserId, userId);
        }
        if (categoryId != null) {
            wrapper.eq(StudyCheckTask::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(StudyCheckTask::getStatus, status);
        }
        wrapper.orderByDesc(StudyCheckTask::getCreateTime);
        Page<StudyCheckTask> pageResult = page(new Page<>(page, pageSize), wrapper);
        return PageResult.of(buildHistoryVO(pageResult.getRecords()), pageResult.getTotal(), page, pageSize);
    }

    @Override
    @Transactional
    public int cleanupExpiredHistory() {
        LocalDateTime cutoff = resolveHistoryCutoff();
        QueryWrapper<StudyCheckTask> wrapper = new QueryWrapper<>();
        wrapper.in("status",
                        StudyConstants.TASK_STATUS_COMPLETED,
                        StudyConstants.TASK_STATUS_TERMINATED,
                        StudyConstants.TASK_STATUS_EXPIRED)
                .and(q -> q.lt("submit_time", cutoff).or().isNull("submit_time").lt("create_time", cutoff));
        List<StudyCheckTask> expiredTasks = list(wrapper);
        if (expiredTasks.isEmpty()) {
            return 0;
        }

        List<Long> taskIds = expiredTasks.stream().map(StudyCheckTask::getId).collect(Collectors.toList());
        Set<Long> userIds = expiredTasks.stream()
                .map(StudyCheckTask::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        aiScoreRecordMapper.delete(new QueryWrapper<StudyAiScoreRecord>().in("task_id", taskIds));
        answerRecordMapper.delete(new QueryWrapper<StudyAnswerRecord>().in("task_id", taskIds));
        taskItemMapper.delete(new QueryWrapper<StudyCheckTaskItem>().in("task_id", taskIds));
        removeBatchByIds(taskIds);

        if (!userIds.isEmpty()) {
          redisService.runAfterCommit(() -> userIds.forEach(redisService::clearStudyUserCache));
        }
        log.info("学习抽查历史保留 {} 天，本次清理任务数 {}", studyCheckRetentionProperties.getHistoryRetentionDays(), taskIds.size());
        return taskIds.size();
    }

    private void validateCreateRequest(StudyTaskCreateRequest request) {
        if ((request.getCheckMode() == StudyConstants.CHECK_MODE_CATEGORY) && request.getCategoryId() == null) {
            throw new BusinessException("专项抽查必须选择分类");
        }
        if (request.getScoringMode() != null
                && !StudyConstants.SCORING_MODE_SELF_ONLY.equals(request.getScoringMode())
                && !StudyConstants.SCORING_MODE_AI_ONLY.equals(request.getScoringMode())
                && !StudyConstants.SCORING_MODE_MIXED.equals(request.getScoringMode())) {
            throw new BusinessException("评分模式不正确");
        }
    }

    private List<StudyQuestion> loadCandidateQuestions(Long userId, StudyTaskCreateRequest request, Integer scoringMode) {
        List<StudyQuestion> questions;
        switch (request.getCheckMode()) {
            case 2:
                questions = queryQuestionsByCategory(request.getCategoryId(), request.getDifficulty());
                break;
            case 3:
                questions = queryQuestionsByProgress(userId, "favorite", request.getDifficulty());
                break;
            case 4:
                questions = queryWeakQuestions(userId, request.getDifficulty());
                break;
            case 5:
                questions = queryQuestionsByProgress(userId, "wrong", request.getDifficulty());
                break;
            case 6:
                List<StudyQuestion> mixed = new ArrayList<>();
                mixed.addAll(queryWeakQuestions(userId, request.getDifficulty()));
                mixed.addAll(queryQuestionsByProgress(userId, "favorite", request.getDifficulty()));
                mixed.addAll(queryQuestionsByProgress(userId, "wrong", request.getDifficulty()));
                questions = deduplicateQuestions(mixed);
                break;
            case 1:
            default:
                questions = queryAllQuestions(request.getDifficulty());
                break;
        }
        if ((defaultInt(request.getOnlyWrongQuestions()) == 1 || defaultInt(request.getOnlyFavorites()) == 1) && !questions.isEmpty()) {
            Map<Long, StudyUserQuestionProgress> progressMap = progressMapper.selectList(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                            .eq(StudyUserQuestionProgress::getUserId, userId)
                            .in(StudyUserQuestionProgress::getQuestionId, questions.stream().map(StudyQuestion::getId).collect(Collectors.toSet())))
                    .stream()
                    .collect(Collectors.toMap(StudyUserQuestionProgress::getQuestionId, item -> item, (left, right) -> left));
            if (defaultInt(request.getOnlyWrongQuestions()) == 1) {
                questions = questions.stream()
                        .filter(item -> {
                            StudyUserQuestionProgress progress = progressMap.get(item.getId());
                            return progress != null && defaultInt(progress.getIsWrongQuestion()) == 1;
                        })
                        .collect(Collectors.toList());
            }
            if (defaultInt(request.getOnlyFavorites()) == 1) {
                questions = questions.stream()
                        .filter(item -> {
                            StudyUserQuestionProgress progress = progressMap.get(item.getId());
                            return progress != null && defaultInt(progress.getIsFavorite()) == 1;
                        })
                        .collect(Collectors.toList());
            }
        }
        if (StudyConstants.SCORING_MODE_AI_ONLY.equals(scoringMode)) {
            questions = questions.stream()
                    .filter(item -> defaultInt(item.getAiScoreEnabled()) == 1)
                    .collect(Collectors.toList());
        }
        return questions;
    }

    private List<StudyQuestion> queryAllQuestions(Integer difficulty) {
        LambdaQueryWrapper<StudyQuestion> wrapper = publishedQuestionWrapper();
        if (difficulty != null) {
            wrapper.eq(StudyQuestion::getDifficulty, difficulty);
        }
        return questionMapper.selectList(wrapper);
    }

    private List<StudyQuestion> queryQuestionsByCategory(Long categoryId, Integer difficulty) {
        LambdaQueryWrapper<StudyQuestion> wrapper = publishedQuestionWrapper().eq(StudyQuestion::getCategoryId, categoryId);
        if (difficulty != null) {
            wrapper.eq(StudyQuestion::getDifficulty, difficulty);
        }
        return questionMapper.selectList(wrapper);
    }

    private List<StudyQuestion> queryQuestionsByProgress(Long userId, String mode, Integer difficulty) {
        LambdaQueryWrapper<StudyUserQuestionProgress> wrapper = new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId);
        if ("favorite".equals(mode)) {
            wrapper.eq(StudyUserQuestionProgress::getIsFavorite, 1);
        } else if ("wrong".equals(mode)) {
            wrapper.eq(StudyUserQuestionProgress::getIsWrongQuestion, 1);
        }
        List<StudyUserQuestionProgress> progressList = progressMapper.selectList(wrapper);
        if (progressList.isEmpty()) {
            return Collections.emptyList();
        }
        List<StudyQuestion> questions = questionMapper.selectBatchIds(progressList.stream()
                        .map(StudyUserQuestionProgress::getQuestionId).collect(Collectors.toSet()))
                .stream().filter(this::isPublishedQuestion).collect(Collectors.toList());
        if (difficulty != null) {
            questions = questions.stream().filter(item -> difficulty.equals(item.getDifficulty())).collect(Collectors.toList());
        }
        return questions;
    }

    private List<StudyQuestion> queryWeakQuestions(Long userId, Integer difficulty) {
        List<StudyUserQuestionProgress> progressList = progressMapper.selectList(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .orderByDesc(StudyUserQuestionProgress::getReviewPriority)
                .orderByAsc(StudyUserQuestionProgress::getMasteryLevel)
                .orderByAsc(StudyUserQuestionProgress::getNextReviewTime));
        if (progressList.isEmpty()) {
            return Collections.emptyList();
        }
        List<StudyQuestion> questions = questionMapper.selectBatchIds(progressList.stream()
                        .map(StudyUserQuestionProgress::getQuestionId).collect(Collectors.toSet()))
                .stream().filter(this::isPublishedQuestion).collect(Collectors.toList());
        Map<Long, StudyUserQuestionProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(StudyUserQuestionProgress::getQuestionId, item -> item));
        questions.sort(Comparator.comparing((StudyQuestion item) -> progressMap.get(item.getId()).getReviewPriority(), Comparator.nullsLast(Integer::compareTo)).reversed()
                .thenComparing(item -> progressMap.get(item.getId()).getMasteryLevel(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(StudyQuestion::getId));
        if (difficulty != null) {
            questions = questions.stream().filter(item -> difficulty.equals(item.getDifficulty())).collect(Collectors.toList());
        }
        return questions;
    }

    private List<StudyQuestion> deduplicateQuestions(List<StudyQuestion> questions) {
        Set<Long> seen = new HashSet<>();
        List<StudyQuestion> result = new ArrayList<>();
        for (StudyQuestion question : questions) {
            if (seen.add(question.getId())) {
                result.add(question);
            }
        }
        return result;
    }

    private LambdaQueryWrapper<StudyQuestion> publishedQuestionWrapper() {
        return new LambdaQueryWrapper<StudyQuestion>()
                .eq(StudyQuestion::getStatus, 1)
                .eq(StudyQuestion::getReviewStatus, 2);
    }

    private boolean isPublishedQuestion(StudyQuestion question) {
        return question != null && defaultInt(question.getStatus()) == 1 && defaultInt(question.getReviewStatus()) == 2;
    }

    private StudyCheckTask getOwnedTask(Long userId, Long taskId) {
        StudyCheckTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("抽查任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该任务");
        }
        return task;
    }

    private StudyCheckTaskItem getOwnedTaskItem(Long userId, Long taskId, Long taskItemId) {
        StudyCheckTask task = getOwnedTask(userId, taskId);
        StudyCheckTaskItem item = taskItemMapper.selectById(taskItemId);
        if (item == null || !item.getTaskId().equals(task.getId())) {
            throw new BusinessException("任务明细不存在");
        }
        return item;
    }

    private AnswerSubmitContext prepareAnswerSubmission(Long userId, StudyCheckTask task, Long taskItemId, StudyAnswerSubmitRequest request) {
        StudyCheckTask latestTask = getById(task.getId());
        if (latestTask == null || !latestTask.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该任务");
        }
        validateTaskCanSubmit(latestTask);

        StudyCheckTaskItem lockedItem = taskItemMapper.selectByIdForUpdate(taskItemId);
        if (lockedItem == null || !lockedItem.getTaskId().equals(latestTask.getId())) {
            throw new BusinessException("任务明细不存在");
        }

        StudyAnswerRecord latestRecord = answerRecordMapper.selectLatestByTaskItemId(taskItemId);
        if (isTaskItemCompleted(lockedItem, latestRecord)) {
            AnswerSubmitContext context = new AnswerSubmitContext();
            context.setExistingResult(loadTaskItemResult(lockedItem.getId()));
            return context;
        }
        if (isTaskItemProcessing(lockedItem, latestRecord)) {
            AnswerSubmitContext context = new AnswerSubmitContext();
            context.setScoringInProgress(true);
            return context;
        }

        StudyQuestion question = questionMapper.selectById(lockedItem.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        boolean allowSelfAssessment = defaultInt(latestTask.getAllowSelfAssessment()) == 1;
        Integer selfAssessmentResult = allowSelfAssessment ? request.getSelfAssessmentResult() : null;
        BigDecimal selfScore = allowSelfAssessment
                ? (request.getSelfScore() != null
                ? request.getSelfScore().setScale(2, RoundingMode.HALF_UP)
                : defaultSelfScore(request.getSelfAssessmentResult()))
                : null;
        String selfComment = allowSelfAssessment ? request.getSelfComment() : null;
        boolean needAi = defaultInt(latestTask.getNeedAiScore()) == 1
                && !StudyConstants.SCORING_MODE_SELF_ONLY.equals(latestTask.getScoringMode())
                && defaultInt(question.getAiScoreEnabled()) == 1
                && defaultInt(request.getTriggerAiScore()) != 0
                && StringUtils.hasText(request.getAnswerContent());
        LocalDateTime submittedTime = LocalDateTime.now();
        Integer maxRound = answerRecordMapper.selectMaxAnswerRound(taskItemId);
        int nextRound = (maxRound == null ? 0 : maxRound) + 1;

        StudyAnswerRecord answerRecord = new StudyAnswerRecord();
        answerRecord.setAnswerNo(buildNo("ANS"));
        answerRecord.setTaskId(latestTask.getId());
        answerRecord.setTaskItemId(taskItemId);
        answerRecord.setUserId(userId);
        answerRecord.setQuestionId(question.getId());
        answerRecord.setAnswerRound(nextRound);
        answerRecord.setAnswerContent(request.getAnswerContent());
        answerRecord.setCharCount(length(request.getAnswerContent()));
        answerRecord.setWordCount(length(removeBlank(request.getAnswerContent())));
        answerRecord.setAnswerLanguage("zh-CN");
        answerRecord.setAnswerSource(1);
        answerRecord.setAnswerStatus(StudyConstants.ANSWER_STATUS_SUBMITTED);
        answerRecord.setSelfAssessmentResult(selfAssessmentResult);
        answerRecord.setSelfScore(selfScore);
        answerRecord.setSelfComment(selfComment);
        answerRecord.setStandardAnswerViewed(request.getStandardAnswerViewed() == null ? defaultInt(lockedItem.getViewAnswerFlag()) : request.getStandardAnswerViewed());
        answerRecord.setStandardAnswerViewTime(lockedItem.getViewAnswerTime());
        answerRecord.setAiScoreStatus(needAi ? StudyConstants.AI_SCORE_STATUS_RUNNING : StudyConstants.AI_SCORE_STATUS_NONE);
        answerRecord.setSubmittedTime(submittedTime);
        answerRecordMapper.insert(answerRecord);

        lockedItem.setAnswerCount(nextRound);
        lockedItem.setStatus(StudyConstants.TASK_ITEM_STATUS_SUBMITTED);
        lockedItem.setViewAnswerFlag(answerRecord.getStandardAnswerViewed());
        lockedItem.setSelfAssessmentResult(selfAssessmentResult);
        lockedItem.setSelfScore(selfScore);
        lockedItem.setLastAnswerId(answerRecord.getId());
        lockedItem.setAnswerDurationSeconds(request.getAnswerDurationSeconds() == null ? 0 : request.getAnswerDurationSeconds());
        lockedItem.setSubmitTime(submittedTime);
        taskItemMapper.updateById(lockedItem);

        AnswerSubmitContext context = new AnswerSubmitContext();
        context.setQuestion(question);
        context.setAnswerRecord(answerRecord);
        context.setSelfAssessmentResult(selfAssessmentResult);
        context.setSelfScore(selfScore);
        context.setNeedAi(needAi);
        return context;
    }

    private StudyCheckTaskItemVO finalizeAnswerSubmission(Long userId,
                                                          StudyCheckTask task,
                                                          AnswerSubmitContext context,
                                                          StudyAiScoreRecord aiScoreRecord,
                                                          BigDecimal aiScore) {
        StudyCheckTaskItem lockedItem = taskItemMapper.selectByIdForUpdate(context.getAnswerRecord().getTaskItemId());
        if (lockedItem == null || !lockedItem.getTaskId().equals(task.getId())) {
            throw new BusinessException("任务明细不存在");
        }
        StudyAnswerRecord storedAnswer = answerRecordMapper.selectById(context.getAnswerRecord().getId());
        if (storedAnswer == null) {
            throw new BusinessException("答题记录不存在");
        }

        BigDecimal finalScore = resolveFinalScore(task.getScoringMode(), context.getSelfScore(), aiScore);
        storedAnswer.setAiScoreStatus(context.isNeedAi()
                ? context.getAnswerRecord().getAiScoreStatus()
                : StudyConstants.AI_SCORE_STATUS_NONE);
        storedAnswer.setFinalScore(finalScore);
        storedAnswer.setAnswerStatus(StudyConstants.ANSWER_STATUS_SCORED);
        answerRecordMapper.updateById(storedAnswer);

        lockedItem.setStatus(StudyConstants.TASK_ITEM_STATUS_SCORED);
        lockedItem.setViewAnswerFlag(storedAnswer.getStandardAnswerViewed());
        lockedItem.setSelfAssessmentResult(context.getSelfAssessmentResult());
        lockedItem.setSelfScore(context.getSelfScore());
        lockedItem.setAiScore(aiScore);
        lockedItem.setFinalScore(finalScore);
        lockedItem.setLastAnswerId(storedAnswer.getId());
        lockedItem.setLastAiScoreRecordId(aiScoreRecord != null ? aiScoreRecord.getId() : null);
        taskItemMapper.updateById(lockedItem);

        syncTaskProgress(task);
        syncUserProgress(userId, context.getQuestion(), storedAnswer, aiScoreRecord);
        clearStudyUserCache(userId);
        return loadTaskItemResult(lockedItem.getId());
    }

    private StudyCheckTaskItemVO loadTaskItemResult(Long taskItemId) {
        StudyCheckTaskItem item = taskItemMapper.selectById(taskItemId);
        if (item == null) {
            return null;
        }
        StudyAiScoreVO aiScoreDetail = item.getLastAiScoreRecordId() == null
                ? null
                : studyAiScoreService.getScoreDetail(item.getLastAiScoreRecordId());
        StudyAnswerRecord latestAnswer = answerRecordMapper.selectLatestByTaskItemId(taskItemId);
        return toTaskItemVO(item, aiScoreDetail, latestAnswer);
    }

    private boolean isTaskItemCompleted(StudyCheckTaskItem item, StudyAnswerRecord latestRecord) {
        return StudyConstants.TASK_ITEM_STATUS_SCORED.equals(item.getStatus())
                || item.getFinalScore() != null
                || (latestRecord != null && StudyConstants.ANSWER_STATUS_SCORED.equals(latestRecord.getAnswerStatus()));
    }

    private boolean isTaskItemProcessing(StudyCheckTaskItem item, StudyAnswerRecord latestRecord) {
        return StudyConstants.TASK_ITEM_STATUS_SUBMITTED.equals(item.getStatus())
                || (latestRecord != null && StudyConstants.ANSWER_STATUS_SUBMITTED.equals(latestRecord.getAnswerStatus()));
    }

    private void validateTaskCanSubmit(StudyCheckTask task) {
        Integer status = task.getStatus();
        if (StudyConstants.TASK_STATUS_COMPLETED.equals(status)) {
            throw new BusinessException("任务已完成，不能继续提交答案");
        }
        if (StudyConstants.TASK_STATUS_TERMINATED.equals(status) || StudyConstants.TASK_STATUS_EXPIRED.equals(status)) {
            throw new BusinessException("当前任务已结束，不能继续提交答案");
        }
    }

    private void syncTaskProgress(StudyCheckTask task) {
        List<StudyCheckTaskItem> items = taskItemMapper.selectList(new LambdaQueryWrapper<StudyCheckTaskItem>()
                .eq(StudyCheckTaskItem::getTaskId, task.getId()));
        int answeredCount = 0;
        int scoredCount = 0;
        int rememberCount = 0;
        int fuzzyCount = 0;
        int forgetCount = 0;
        int excellentCount = 0;
        int passCount = 0;
        int failedCount = 0;
        BigDecimal selfScore = BigDecimal.ZERO;
        BigDecimal aiScore = BigDecimal.ZERO;
        BigDecimal finalScore = BigDecimal.ZERO;
        for (StudyCheckTaskItem item : items) {
            if (item.getSubmitTime() != null) {
                answeredCount++;
            }
            if (item.getFinalScore() != null || item.getSelfScore() != null || item.getAiScore() != null) {
                scoredCount++;
            }
            if (item.getSelfAssessmentResult() != null) {
                if (item.getSelfAssessmentResult() == StudyConstants.SELF_RESULT_REMEMBER) {
                    rememberCount++;
                } else if (item.getSelfAssessmentResult() == StudyConstants.SELF_RESULT_FUZZY) {
                    fuzzyCount++;
                } else if (item.getSelfAssessmentResult() == StudyConstants.SELF_RESULT_FORGET) {
                    forgetCount++;
                }
            }
            if (item.getSelfScore() != null) {
                selfScore = selfScore.add(item.getSelfScore());
            }
            if (item.getAiScore() != null) {
                aiScore = aiScore.add(item.getAiScore());
            }
            BigDecimal itemScore = item.getFinalScore() != null ? item.getFinalScore() : item.getSelfScore();
            if (itemScore != null) {
                finalScore = finalScore.add(itemScore);
                if (itemScore.compareTo(new BigDecimal("90")) >= 0) {
                    excellentCount++;
                }
                if (item.getScorePassMark() != null && itemScore.compareTo(item.getScorePassMark()) >= 0) {
                    passCount++;
                } else {
                    failedCount++;
                }
            }
        }
        StudyCheckTask update = new StudyCheckTask();
        update.setId(task.getId());
        update.setAnsweredCount(answeredCount);
        update.setScoredCount(scoredCount);
        update.setRememberCount(rememberCount);
        update.setFuzzyCount(fuzzyCount);
        update.setForgetCount(forgetCount);
        update.setExcellentCount(excellentCount);
        update.setPassCount(passCount);
        update.setFailedCount(failedCount);
        update.setSelfScore(selfScore.setScale(2, RoundingMode.HALF_UP));
        update.setAiScore(aiScore.setScale(2, RoundingMode.HALF_UP));
        update.setFinalScore(finalScore.setScale(2, RoundingMode.HALF_UP));
        updateById(update);
    }

    private void syncUserProgress(Long userId, StudyQuestion question, StudyAnswerRecord answerRecord, StudyAiScoreRecord aiScoreRecord) {
        StudyUserQuestionProgress progress = progressMapper.selectOne(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getQuestionId, question.getId())
                .last("LIMIT 1"));
        if (progress == null) {
            progress = new StudyUserQuestionProgress();
            progress.setUserId(userId);
            progress.setQuestionId(question.getId());
            progress.setStudyStatus(StudyConstants.STUDY_STATUS_LEARNING);
            progress.setMasteryLevel(0);
            progress.setReviewPriority(3);
            progress.setStudyCount(0);
            progress.setCheckCount(0);
            progress.setRememberCount(0);
            progress.setFuzzyCount(0);
            progress.setForgetCount(0);
            progress.setWrongCount(0);
            progress.setIsFavorite(0);
            progress.setIsWrongQuestion(0);
            progress.setNoteCount(0);
        }
        progress.setCheckCount(defaultInt(progress.getCheckCount()) + 1);
        progress.setLastResult(answerRecord.getSelfAssessmentResult());
        progress.setLastScore(answerRecord.getFinalScore());
        progress.setLastAnswerId(answerRecord.getId());
        progress.setLastAiScoreRecordId(aiScoreRecord != null ? aiScoreRecord.getId() : null);
        progress.setLastCheckTime(LocalDateTime.now());
        if (answerRecord.getSelfAssessmentResult() != null) {
            if (answerRecord.getSelfAssessmentResult() == StudyConstants.SELF_RESULT_REMEMBER) {
                progress.setRememberCount(defaultInt(progress.getRememberCount()) + 1);
                progress.setMasteryLevel(Math.min(5, defaultInt(progress.getMasteryLevel()) + 1));
            } else if (answerRecord.getSelfAssessmentResult() == StudyConstants.SELF_RESULT_FUZZY) {
                progress.setFuzzyCount(defaultInt(progress.getFuzzyCount()) + 1);
            } else if (answerRecord.getSelfAssessmentResult() == StudyConstants.SELF_RESULT_FORGET) {
                progress.setForgetCount(defaultInt(progress.getForgetCount()) + 1);
                progress.setWrongCount(defaultInt(progress.getWrongCount()) + 1);
                progress.setIsWrongQuestion(1);
            }
        }
        BigDecimal finalScore = answerRecord.getFinalScore();
        if (finalScore != null) {
            progress.setBestScore(progress.getBestScore() == null ? finalScore : progress.getBestScore().max(finalScore));
            progress.setAvgScore(calculateAverage(progress.getAvgScore(), defaultInt(progress.getCheckCount()), finalScore));
            if (question.getScorePassMark() != null && finalScore.compareTo(question.getScorePassMark()) < 0) {
                progress.setWrongCount(defaultInt(progress.getWrongCount()) + 1);
                progress.setIsWrongQuestion(1);
            } else if (answerRecord.getSelfAssessmentResult() != null && answerRecord.getSelfAssessmentResult() == StudyConstants.SELF_RESULT_REMEMBER) {
                progress.setIsWrongQuestion(0);
            }
        }
        progress.setReviewPriority(resolveReviewPriority(progress));
        progress.setNextReviewTime(resolveNextReviewTime(progress));
        if (progress.getId() == null) {
            progressMapper.insert(progress);
        } else {
            progressMapper.updateById(progress);
        }
        StudyQuestion update = new StudyQuestion();
        update.setId(question.getId());
        update.setCheckCount(defaultInt(question.getCheckCount()) + 1);
        questionMapper.updateById(update);
    }

    private List<StudyCheckHistoryVO> buildHistoryVO(List<StudyCheckTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> categoryIds = tasks.stream()
                .map(StudyCheckTask::getCategoryId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, StudyCategory> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(StudyCategory::getId, item -> item, (left, right) -> left));
        Set<Long> userIds = tasks.stream()
                .map(StudyCheckTask::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left));
        return tasks.stream().map(task -> {
            StudyCheckHistoryVO vo = new StudyCheckHistoryVO();
            BeanUtils.copyProperties(task, vo);
            StudyCategory category = categoryMap.get(task.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
            User user = userMap.get(task.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, StudyAiScoreVO> loadAiScoreMap(List<StudyCheckTaskItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> scoreIds = items.stream()
                .map(StudyCheckTaskItem::getLastAiScoreRecordId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (scoreIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, StudyAiScoreVO> scoreMap = new HashMap<>();
        for (StudyAiScoreRecord record : aiScoreRecordMapper.selectBatchIds(scoreIds)) {
            StudyAiScoreVO vo = new StudyAiScoreVO();
            BeanUtils.copyProperties(record, vo);
            scoreMap.put(record.getId(), vo);
        }
        return scoreMap;
    }

    private Map<Long, StudyAnswerRecord> loadLatestAnswerMap(List<StudyCheckTaskItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, StudyAnswerRecord> answerMap = new HashMap<>();
        for (StudyCheckTaskItem item : items) {
            if (item == null || item.getId() == null || defaultInt(item.getAnswerCount()) <= 0) {
                continue;
            }
            StudyAnswerRecord latestAnswer = answerRecordMapper.selectLatestByTaskItemId(item.getId());
            if (latestAnswer != null) {
                answerMap.put(item.getId(), latestAnswer);
            }
        }
        return answerMap;
    }

    private StudyCheckTaskItemVO toTaskItemVO(StudyCheckTaskItem item, StudyAiScoreVO aiScoreVO, StudyAnswerRecord answerRecord) {
        StudyCheckTaskItemVO vo = new StudyCheckTaskItemVO();
        BeanUtils.copyProperties(item, vo);
        if (answerRecord != null) {
            vo.setAnswerContent(answerRecord.getAnswerContent());
            vo.setSelfComment(answerRecord.getSelfComment());
        }
        vo.setAiScoreDetail(aiScoreVO);
        return vo;
    }

    private LocalDateTime resolveHistoryCutoff() {
        long retentionDays = Math.max(1L, studyCheckRetentionProperties.getHistoryRetentionDays());
        return LocalDateTime.now().minusDays(retentionDays);
    }

    private String buildFilterJson(StudyTaskCreateRequest request, Integer scoringMode, Integer needAiScore, Integer allowSelfAssessment) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("checkMode", request.getCheckMode());
        filter.put("categoryId", request.getCategoryId());
        filter.put("difficulty", request.getDifficulty());
        filter.put("questionCount", request.getQuestionCount());
        filter.put("needAiScore", needAiScore);
        filter.put("allowSelfAssessment", allowSelfAssessment);
        filter.put("showStandardAnswer", request.getShowStandardAnswer());
        filter.put("scoringMode", scoringMode);
        filter.put("onlyWrongQuestions", request.getOnlyWrongQuestions());
        filter.put("onlyFavorites", request.getOnlyFavorites());
        try {
            return objectMapper.writeValueAsString(filter);
        } catch (Exception e) {
            return "{}";
        }
    }

    private BigDecimal defaultSelfScore(Integer selfAssessmentResult) {
        if (selfAssessmentResult == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (selfAssessmentResult == StudyConstants.SELF_RESULT_REMEMBER) {
            return StudyConstants.SELF_SCORE_REMEMBER.setScale(2, RoundingMode.HALF_UP);
        }
        if (selfAssessmentResult == StudyConstants.SELF_RESULT_FUZZY) {
            return StudyConstants.SELF_SCORE_FUZZY.setScale(2, RoundingMode.HALF_UP);
        }
        return StudyConstants.SELF_SCORE_FORGET.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveFinalScore(Integer scoringMode, BigDecimal selfScore, BigDecimal aiScore) {
        BigDecimal safeSelfScore = selfScore == null ? BigDecimal.ZERO : selfScore;
        BigDecimal safeAiScore = aiScore == null ? BigDecimal.ZERO : aiScore;
        if (StudyConstants.SCORING_MODE_SELF_ONLY.equals(scoringMode)) {
            return safeSelfScore.setScale(2, RoundingMode.HALF_UP);
        }
        if (StudyConstants.SCORING_MODE_AI_ONLY.equals(scoringMode)) {
            return safeAiScore.setScale(2, RoundingMode.HALF_UP);
        }
        if (selfScore != null && aiScore != null) {
            return selfScore.add(aiScore).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        }
        if (selfScore != null) {
            return selfScore.setScale(2, RoundingMode.HALF_UP);
        }
        if (aiScore != null) {
            return aiScore.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private Integer resolveReviewPriority(StudyUserQuestionProgress progress) {
        if (progress == null) {
            return 3;
        }
        if (defaultInt(progress.getIsWrongQuestion()) == 1 || StudyConstants.SELF_RESULT_FORGET.equals(progress.getLastResult())) {
            return 5;
        }
        if (StudyConstants.SELF_RESULT_FUZZY.equals(progress.getLastResult())) {
            return 4;
        }
        int masteryLevel = defaultInt(progress.getMasteryLevel());
        if (masteryLevel <= 1) {
            return 4;
        }
        if (masteryLevel >= 4) {
            return 1;
        }
        return Math.max(1, 5 - masteryLevel);
    }

    private LocalDateTime resolveNextReviewTime(StudyUserQuestionProgress progress) {
        LocalDateTime baseTime = progress.getLastCheckTime() == null ? LocalDateTime.now() : progress.getLastCheckTime();
        if (defaultInt(progress.getIsWrongQuestion()) == 1 || StudyConstants.SELF_RESULT_FORGET.equals(progress.getLastResult())) {
            return baseTime.plusDays(1);
        }
        if (StudyConstants.SELF_RESULT_FUZZY.equals(progress.getLastResult())) {
            return baseTime.plusDays(3);
        }
        int masteryLevel = defaultInt(progress.getMasteryLevel());
        if (masteryLevel >= 5) {
            return baseTime.plusDays(30);
        }
        if (masteryLevel == 4) {
            return baseTime.plusDays(14);
        }
        if (masteryLevel == 3) {
            return baseTime.plusDays(7);
        }
        if (masteryLevel == 2) {
            return baseTime.plusDays(3);
        }
        return baseTime.plusDays(1);
    }

    private BigDecimal calculateAverage(BigDecimal currentAverage, int totalCount, BigDecimal newScore) {
        if (newScore == null) {
            return currentAverage == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : currentAverage.setScale(2, RoundingMode.HALF_UP);
        }
        if (totalCount <= 1 || currentAverage == null) {
            return newScore.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal previousTotal = currentAverage.multiply(BigDecimal.valueOf(totalCount - 1L));
        return previousTotal.add(newScore)
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultScore(BigDecimal score) {
        return (score == null ? new BigDecimal("100") : score).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultPassScore(BigDecimal score) {
        return (score == null ? new BigDecimal("60") : score).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal readDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        String text = value.toString().trim();
        if (!StringUtils.hasText(text)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer normalizeScoringMode(Integer scoringMode) {
        if (StudyConstants.SCORING_MODE_SELF_ONLY.equals(scoringMode)
                || StudyConstants.SCORING_MODE_AI_ONLY.equals(scoringMode)
                || StudyConstants.SCORING_MODE_MIXED.equals(scoringMode)) {
            return scoringMode;
        }
        return StudyConstants.SCORING_MODE_MIXED;
    }

    private Integer resolveNeedAiScore(Integer scoringMode) {
        return StudyConstants.SCORING_MODE_SELF_ONLY.equals(scoringMode) ? StudyConstants.NO : StudyConstants.YES;
    }

    private Integer resolveAllowSelfAssessment(Integer scoringMode) {
        return StudyConstants.SCORING_MODE_AI_ONLY.equals(scoringMode) ? StudyConstants.NO : StudyConstants.YES;
    }

    private Map<String, Object> firstValidRow(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        for (Map<String, Object> row : rows) {
            if (row != null) {
                return row;
            }
        }
        return null;
    }

    private void clearStudyUserCache(Long userId) {
        redisService.runAfterCommit(() -> redisService.clearStudyUserCache(userId));
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int length(String text) {
        return text == null ? 0 : text.length();
    }

    private String removeBlank(String text) {
        return text == null ? null : text.replaceAll("\\s+", "");
    }

    private String buildNo(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    private static class AnswerSubmitContext {

        private StudyQuestion question;
        private StudyAnswerRecord answerRecord;
        private Integer selfAssessmentResult;
        private BigDecimal selfScore;
        private boolean needAi;
        private boolean scoringInProgress;
        private StudyCheckTaskItemVO existingResult;

        public StudyQuestion getQuestion() {
            return question;
        }

        public void setQuestion(StudyQuestion question) {
            this.question = question;
        }

        public StudyAnswerRecord getAnswerRecord() {
            return answerRecord;
        }

        public void setAnswerRecord(StudyAnswerRecord answerRecord) {
            this.answerRecord = answerRecord;
        }

        public Integer getSelfAssessmentResult() {
            return selfAssessmentResult;
        }

        public void setSelfAssessmentResult(Integer selfAssessmentResult) {
            this.selfAssessmentResult = selfAssessmentResult;
        }

        public BigDecimal getSelfScore() {
            return selfScore;
        }

        public void setSelfScore(BigDecimal selfScore) {
            this.selfScore = selfScore;
        }

        public boolean isNeedAi() {
            return needAi;
        }

        public void setNeedAi(boolean needAi) {
            this.needAi = needAi;
        }

        public boolean isScoringInProgress() {
            return scoringInProgress;
        }

        public void setScoringInProgress(boolean scoringInProgress) {
            this.scoringInProgress = scoringInProgress;
        }

        public StudyCheckTaskItemVO getExistingResult() {
            return existingResult;
        }

        public void setExistingResult(StudyCheckTaskItemVO existingResult) {
            this.existingResult = existingResult;
        }
    }
}
