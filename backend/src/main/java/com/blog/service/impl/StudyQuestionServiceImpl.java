package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.constant.StudyConstants;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.PageResult;
import com.blog.config.StudyCacheProperties;
import com.blog.dto.request.StudyQuestionNoteRequest;
import com.blog.dto.request.StudyQuestionSaveRequest;
import com.blog.dto.request.StudyQuestionStatusRequest;
import com.blog.dto.response.StudyNoteVO;
import com.blog.dto.response.StudyProgressOverviewVO;
import com.blog.dto.response.StudyQuestionDetailVO;
import com.blog.dto.response.StudyQuestionListVO;
import com.blog.entity.StudyCategory;
import com.blog.entity.StudyCheckTask;
import com.blog.entity.StudyQuestion;
import com.blog.entity.StudyQuestionVersion;
import com.blog.entity.StudyUserQuestionNote;
import com.blog.entity.StudyUserQuestionProgress;
import com.blog.mapper.StudyCategoryMapper;
import com.blog.mapper.StudyCheckTaskMapper;
import com.blog.mapper.StudyQuestionMapper;
import com.blog.mapper.StudyQuestionVersionMapper;
import com.blog.mapper.StudyUserQuestionNoteMapper;
import com.blog.mapper.StudyUserQuestionProgressMapper;
import com.blog.service.RedisService;
import com.blog.service.StudyCategoryService;
import com.blog.service.StudyQuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class StudyQuestionServiceImpl extends ServiceImpl<StudyQuestionMapper, StudyQuestion> implements StudyQuestionService {

    private final StudyCategoryMapper studyCategoryMapper;
    private final StudyUserQuestionProgressMapper progressMapper;
    private final StudyUserQuestionNoteMapper noteMapper;
    private final StudyQuestionVersionMapper questionVersionMapper;
    private final StudyCategoryService studyCategoryService;
    private final StudyCheckTaskMapper studyCheckTaskMapper;
    private final RedisService redisService;
    private final StudyCacheProperties studyCacheProperties;

    public StudyQuestionServiceImpl(StudyCategoryMapper studyCategoryMapper,
                                    StudyUserQuestionProgressMapper progressMapper,
                                    StudyUserQuestionNoteMapper noteMapper,
                                    StudyQuestionVersionMapper questionVersionMapper,
                                    StudyCategoryService studyCategoryService,
                                    StudyCheckTaskMapper studyCheckTaskMapper,
                                    RedisService redisService,
                                    StudyCacheProperties studyCacheProperties) {
        this.studyCategoryMapper = studyCategoryMapper;
        this.progressMapper = progressMapper;
        this.noteMapper = noteMapper;
        this.questionVersionMapper = questionVersionMapper;
        this.studyCategoryService = studyCategoryService;
        this.studyCheckTaskMapper = studyCheckTaskMapper;
        this.redisService = redisService;
        this.studyCacheProperties = studyCacheProperties;
    }

    @Override
    public PageResult<StudyQuestionListVO> getQuestionPage(Long userId, Integer page, Integer pageSize,
                                                           Long categoryId, String keyword, Integer difficulty,
                                                           Integer studyStatus, Integer isFavorite,
                                                           String studyCountSort) {
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyQuestion::getStatus, StudyConstants.STATUS_ENABLED)
                .eq(StudyQuestion::getReviewStatus, StudyConstants.REVIEW_PUBLISHED);
        if (categoryId != null) {
            Set<Long> categoryIds = resolveCategoryIds(categoryId);
            if (categoryIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, page, pageSize);
            }
            wrapper.in(StudyQuestion::getCategoryId, categoryIds);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(StudyQuestion::getTitle, keyword)
                    .or().like(StudyQuestion::getKeywords, keyword)
                    .or().like(StudyQuestion::getAnswerSummary, keyword));
        }
        if (difficulty != null) {
            wrapper.eq(StudyQuestion::getDifficulty, difficulty);
        }
        if (userId != null && (studyStatus != null || isFavorite != null)) {
            List<StudyUserQuestionProgress> progressList = progressMapper.selectList(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                    .eq(StudyUserQuestionProgress::getUserId, userId)
                    .eq(studyStatus != null, StudyUserQuestionProgress::getStudyStatus, studyStatus)
                    .eq(isFavorite != null, StudyUserQuestionProgress::getIsFavorite, isFavorite));
            Set<Long> questionIds = progressList.stream()
                    .map(StudyUserQuestionProgress::getQuestionId)
                    .collect(Collectors.toSet());
            if (questionIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, page, pageSize);
            }
            wrapper.in(StudyQuestion::getId, questionIds);
        } else if (userId == null && (studyStatus != null || Integer.valueOf(1).equals(isFavorite))) {
            return PageResult.of(Collections.emptyList(), 0L, page, pageSize);
        }
        String normalizedStudyCountSort = normalizeStudyCountSort(studyCountSort);
        if ("asc".equals(normalizedStudyCountSort)) {
            wrapper.orderByAsc(StudyQuestion::getStudyCount);
        } else if ("desc".equals(normalizedStudyCountSort)) {
            wrapper.orderByDesc(StudyQuestion::getStudyCount);
        }
        wrapper.orderByDesc(StudyQuestion::getQuestionNo)
                .orderByDesc(StudyQuestion::getUpdateTime);

        Page<StudyQuestion> pageResult = page(new Page<>(page, pageSize), wrapper);
        List<StudyQuestion> records = pageResult.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(Collections.emptyList(), pageResult.getTotal(), page, pageSize);
        }
        Map<Long, StudyCategory> categoryMap = studyCategoryMapper.selectBatchIds(records.stream()
                        .map(StudyQuestion::getCategoryId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(StudyCategory::getId, item -> item));
        Map<Long, StudyUserQuestionProgress> progressMap = loadProgressMap(userId, records.stream().map(StudyQuestion::getId).collect(Collectors.toSet()));

        List<StudyQuestionListVO> list = records.stream()
                .map(question -> toListVO(question, categoryMap.get(question.getCategoryId()), progressMap.get(question.getId())))
                .filter(vo -> studyStatus == null || studyStatus.equals(vo.getStudyStatus()))
                .filter(vo -> isFavorite == null || isFavorite.equals(vo.getIsFavorite()))
                .collect(Collectors.toList());
        return PageResult.of(list, pageResult.getTotal(), page, pageSize);
    }

    @Override
    @Transactional
    public StudyQuestionDetailVO getQuestionDetail(Long userId, Long questionId) {
        StudyQuestion question = getPublishedQuestion(questionId);
        StudyCategory category = studyCategoryMapper.selectById(question.getCategoryId());
        StudyUserQuestionProgress progress = getProgress(userId, questionId, false);
        List<StudyNoteVO> notes = listNotes(userId, questionId);

        StudyQuestion update = new StudyQuestion();
        update.setId(questionId);
        update.setViewCount((question.getViewCount() == null ? 0 : question.getViewCount()) + 1);
        updateById(update);
        question.setViewCount(update.getViewCount());

        StudyQuestionDetailVO vo = new StudyQuestionDetailVO();
        BeanUtils.copyProperties(question, vo);
        vo.setCategoryName(category != null ? category.getCategoryName() : null);
        fillProgress(vo, progress);
        vo.setNotes(notes);
        return vo;
    }

    @Override
    public StudyQuestionDetailVO getRandomQuestion(Long userId, Long categoryId, Integer difficulty,
                                                   Boolean preferPendingReview, Boolean preferLowStudyCount,
                                                   String excludeIds) {
        Set<Long> excludedQuestionIds = parseQuestionIds(excludeIds);
        List<StudyQuestion> candidates = listRandomCandidates(categoryId, difficulty, excludedQuestionIds);
        if (candidates.isEmpty() && !excludedQuestionIds.isEmpty()) {
            candidates = listRandomCandidates(categoryId, difficulty, Collections.emptySet());
        }
        if (candidates.isEmpty()) {
            throw new BusinessException("没有匹配的题目");
        }
        List<StudyQuestion> preferredCandidates = filterPreferredRandomCandidates(userId, preferPendingReview, candidates);
        List<StudyQuestion> finalCandidates = preferredCandidates.isEmpty() ? candidates : preferredCandidates;
        StudyQuestion question = pickRandomQuestion(finalCandidates, preferLowStudyCount);
        return getQuestionDetail(userId, question.getId());
    }

    @Override
    @Transactional
    public void recordStudy(Long userId, Long questionId) {
        StudyQuestion question = getPublishedQuestion(questionId);
        StudyUserQuestionProgress progress = getProgress(userId, questionId, true);
        LocalDateTime now = LocalDateTime.now();
        if (progress.getFirstStudyTime() == null) {
            progress.setFirstStudyTime(now);
        }
        progress.setLastStudyTime(now);
        progress.setStudyStatus(progress.getStudyStatus() == null || progress.getStudyStatus() == 0
                ? StudyConstants.STUDY_STATUS_LEARNING : progress.getStudyStatus());
        progress.setStudyCount(defaultInt(progress.getStudyCount()) + 1);
        saveOrUpdateProgress(progress);

        question.setStudyCount(defaultInt(question.getStudyCount()) + 1);
        updateById(question);
        clearStudyUserCache(userId);
    }

    @Override
    @Transactional
    public void toggleFavorite(Long userId, Long questionId, Integer favorite) {
        getPublishedQuestion(questionId);
        StudyUserQuestionProgress progress = getProgress(userId, questionId, true);
        progress.setIsFavorite(favorite != null ? favorite : (defaultInt(progress.getIsFavorite()) == 1 ? 0 : 1));
        saveOrUpdateProgress(progress);
        clearStudyUserCache(userId);
    }

    @Override
    @Transactional
    public StudyNoteVO saveNote(Long userId, Long questionId, StudyQuestionNoteRequest request) {
        getPublishedQuestion(questionId);
        StudyUserQuestionNote note = new StudyUserQuestionNote();
        note.setUserId(userId);
        note.setQuestionId(questionId);
        note.setNoteType(request.getNoteType());
        note.setContent(request.getContent());
        note.setIsPinned(request.getIsPinned() == null ? 0 : request.getIsPinned());
        noteMapper.insert(note);

        StudyUserQuestionProgress progress = getProgress(userId, questionId, true);
        progress.setNoteCount(defaultInt(progress.getNoteCount()) + 1);
        saveOrUpdateProgress(progress);
        clearStudyUserCache(userId);

        StudyNoteVO vo = new StudyNoteVO();
        BeanUtils.copyProperties(note, vo);
        return vo;
    }

    @Override
    public List<StudyNoteVO> listNotes(Long userId, Long questionId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<StudyUserQuestionNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyUserQuestionNote::getUserId, userId)
                .eq(StudyUserQuestionNote::getQuestionId, questionId)
                .orderByDesc(StudyUserQuestionNote::getIsPinned)
                .orderByDesc(StudyUserQuestionNote::getUpdateTime);
        return noteMapper.selectList(wrapper).stream().map(item -> {
            StudyNoteVO vo = new StudyNoteVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateProgressStatus(Long userId, Long questionId, StudyQuestionStatusRequest request) {
        getPublishedQuestion(questionId);
        StudyUserQuestionProgress progress = getProgress(userId, questionId, true);
        progress.setStudyStatus(request.getStudyStatus());
        progress.setMasteryLevel(request.getMasteryLevel());
        progress.setReviewPriority(request.getReviewPriority() == null ? defaultReviewPriority(request.getMasteryLevel()) : request.getReviewPriority());
        saveOrUpdateProgress(progress);
        clearStudyUserCache(userId);
    }

    @Override
    public StudyProgressOverviewVO getOverview(Long userId) {
        if (userId != null) {
            StudyProgressOverviewVO cached = redisService.get(redisService.buildStudyOverviewCacheKey(userId), StudyProgressOverviewVO.class);
            if (cached != null) {
                return cached;
            }
        }
        StudyProgressOverviewVO vo = new StudyProgressOverviewVO();
        vo.setTotalQuestions(count(new LambdaQueryWrapper<StudyQuestion>()
                .eq(StudyQuestion::getStatus, 1)
                .eq(StudyQuestion::getReviewStatus, 2)));
        vo.setStudiedQuestions(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .gt(StudyUserQuestionProgress::getStudyCount, 0)));
        vo.setMasteredQuestions(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getStudyStatus, StudyConstants.STUDY_STATUS_MASTERED)));
        vo.setReviewingQuestions(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getStudyStatus, StudyConstants.STUDY_STATUS_REVIEW)));
        vo.setFavoriteQuestions(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getIsFavorite, 1)));
        vo.setWrongQuestions(progressMapper.selectCount(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getIsWrongQuestion, 1)));
        vo.setCheckTaskCount(studyCheckTaskMapper.selectCount(new LambdaQueryWrapper<StudyCheckTask>()
                .eq(StudyCheckTask::getUserId, userId)));
        vo.setCompletedCheckTaskCount(studyCheckTaskMapper.selectCount(new LambdaQueryWrapper<StudyCheckTask>()
                .eq(StudyCheckTask::getUserId, userId)
                .eq(StudyCheckTask::getStatus, StudyConstants.TASK_STATUS_COMPLETED)));
        QueryWrapper<StudyUserQuestionProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("AVG(avg_score) avgScore").eq("user_id", userId);
        List<Map<String, Object>> avgRows = progressMapper.selectMaps(queryWrapper);
        Map<String, Object> avgRow = firstValidRow(avgRows);
        if (avgRow != null && avgRow.get("avgScore") != null) {
            vo.setAvgScore(new BigDecimal(avgRow.get("avgScore").toString()).setScale(2, RoundingMode.HALF_UP));
        } else {
            vo.setAvgScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (userId != null) {
            redisService.setWithMinutes(redisService.buildStudyOverviewCacheKey(userId), vo, studyCacheProperties.getOverviewMinutes());
        }
        return vo;
    }

    @Override
    @Transactional
    public StudyQuestion saveQuestion(StudyQuestionSaveRequest request, Long operatorId) {
        validateQuestionRequest(request, null);
        StudyQuestion question = new StudyQuestion();
        fillQuestion(question, request, operatorId);
        question.setVersionNo(1);
        question.setCreateBy(operatorId);
        question.setUpdateBy(operatorId);
        save(question);
        insertVersion(question, 1, "后台新增题目", operatorId);
        studyCategoryService.updateQuestionCount(question.getCategoryId());
        clearAllStudyOverviewCacheAfterCommit();
        return question;
    }

    @Override
    @Transactional
    public StudyQuestion updateQuestion(Long id, StudyQuestionSaveRequest request, Long operatorId) {
        StudyQuestion question = getById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        Long oldCategoryId = question.getCategoryId();
        validateQuestionRequest(request, id);
        fillQuestion(question, request, operatorId);
        question.setVersionNo(defaultInt(question.getVersionNo()) + 1);
        question.setUpdateBy(operatorId);
        updateById(question);
        insertVersion(question, 2, "后台更新题目", operatorId);
        studyCategoryService.updateQuestionCount(question.getCategoryId());
        if (!Objects.equals(oldCategoryId, question.getCategoryId())) {
            studyCategoryService.updateQuestionCount(oldCategoryId);
        }
        clearAllStudyOverviewCacheAfterCommit();
        return question;
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id, Long operatorId) {
        StudyQuestion question = getById(id);
        if (question == null) {
            return;
        }
        removeById(id);
        insertVersion(question, 5, "后台删除题目", operatorId);
        studyCategoryService.updateQuestionCount(question.getCategoryId());
        clearAllStudyOverviewCacheAfterCommit();
    }

    private StudyQuestion getPublishedQuestion(Long questionId) {
        StudyQuestion question = getById(questionId);
        if (question == null || defaultInt(question.getStatus()) != 1 || defaultInt(question.getReviewStatus()) != 2) {
            throw new BusinessException("题目不存在或未发布");
        }
        return question;
    }

    private List<StudyQuestion> listRandomCandidates(Long categoryId, Integer difficulty, Set<Long> excludedQuestionIds) {
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyQuestion::getStatus, StudyConstants.STATUS_ENABLED)
                .eq(StudyQuestion::getReviewStatus, StudyConstants.REVIEW_PUBLISHED);
        if (categoryId != null) {
            Set<Long> categoryIds = resolveCategoryIds(categoryId);
            if (categoryIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(StudyQuestion::getCategoryId, categoryIds);
        }
        if (difficulty != null) {
            wrapper.eq(StudyQuestion::getDifficulty, difficulty);
        }
        if (excludedQuestionIds != null && !excludedQuestionIds.isEmpty()) {
            wrapper.notIn(StudyQuestion::getId, excludedQuestionIds);
        }
        return list(wrapper);
    }

    private List<StudyQuestion> filterPreferredRandomCandidates(Long userId, Boolean preferPendingReview,
                                                                List<StudyQuestion> candidates) {
        if (!Boolean.TRUE.equals(preferPendingReview) || userId == null || candidates.isEmpty()) {
            return candidates;
        }
        Map<Long, StudyUserQuestionProgress> progressMap = loadProgressMap(userId, candidates.stream()
                .map(StudyQuestion::getId)
                .collect(Collectors.toSet()));
        return candidates.stream()
                .filter(question -> {
                    StudyUserQuestionProgress progress = progressMap.get(question.getId());
                    Integer studyStatus = progress == null ? StudyConstants.STUDY_STATUS_NOT_STARTED : progress.getStudyStatus();
                    return studyStatus == null
                            || Objects.equals(studyStatus, StudyConstants.STUDY_STATUS_NOT_STARTED)
                            || Objects.equals(studyStatus, StudyConstants.STUDY_STATUS_REVIEW);
                })
                .collect(Collectors.toList());
    }

    private StudyQuestion pickRandomQuestion(List<StudyQuestion> candidates, Boolean preferLowStudyCount) {
        if (candidates.isEmpty()) {
            throw new BusinessException("没有匹配的题目");
        }
        if (!Boolean.TRUE.equals(preferLowStudyCount) || candidates.size() == 1) {
            return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        }
        List<StudyQuestion> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort(Comparator.comparingInt((StudyQuestion item) -> defaultInt(item.getStudyCount()))
                .thenComparing(StudyQuestion::getId));
        int preferredSize = Math.min(sortedCandidates.size(), Math.max(1, (int) Math.ceil(sortedCandidates.size() * 0.3D)));
        preferredSize = Math.min(preferredSize, 12);
        return sortedCandidates.get(ThreadLocalRandom.current().nextInt(preferredSize));
    }

    private Set<Long> parseQuestionIds(String questionIdsText) {
        if (!StringUtils.hasText(questionIdsText)) {
            return Collections.emptySet();
        }
        Set<Long> questionIds = new HashSet<>();
        for (String part : questionIdsText.split(",")) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            try {
                questionIds.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
                // 忽略无效题目ID，避免历史缓存异常导致接口不可用
            }
        }
        return questionIds;
    }

    private Set<Long> resolveCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptySet();
        }
        StudyCategory category = studyCategoryMapper.selectById(categoryId);
        if (category == null || !Objects.equals(category.getStatus(), StudyConstants.STATUS_ENABLED)) {
            return Collections.emptySet();
        }
        if (!StringUtils.hasText(category.getCategoryPath())) {
            return Collections.singleton(categoryId);
        }
        return studyCategoryMapper.selectList(new LambdaQueryWrapper<StudyCategory>()
                        .select(StudyCategory::getId)
                        .eq(StudyCategory::getStatus, StudyConstants.STATUS_ENABLED)
                        .and(wrapper -> wrapper.eq(StudyCategory::getCategoryPath, category.getCategoryPath())
                                .or()
                                .likeRight(StudyCategory::getCategoryPath, category.getCategoryPath() + "/")))
                .stream()
                .map(StudyCategory::getId)
                .collect(Collectors.toSet());
    }

    private String normalizeStudyCountSort(String studyCountSort) {
        if (!StringUtils.hasText(studyCountSort)) {
            return null;
        }
        String normalized = studyCountSort.trim().toLowerCase();
        if ("asc".equals(normalized) || "desc".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private Map<Long, StudyUserQuestionProgress> loadProgressMap(Long userId, java.util.Set<Long> questionIds) {
        if (userId == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return progressMapper.selectList(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                        .eq(StudyUserQuestionProgress::getUserId, userId)
                        .in(StudyUserQuestionProgress::getQuestionId, questionIds))
                .stream().collect(Collectors.toMap(StudyUserQuestionProgress::getQuestionId, item -> item));
    }

    private StudyQuestionListVO toListVO(StudyQuestion question, StudyCategory category, StudyUserQuestionProgress progress) {
        StudyQuestionListVO vo = new StudyQuestionListVO();
        BeanUtils.copyProperties(question, vo);
        vo.setCategoryName(category != null ? category.getCategoryName() : null);
        if (progress != null) {
            vo.setStudyStatus(progress.getStudyStatus());
            vo.setMasteryLevel(progress.getMasteryLevel());
            vo.setReviewPriority(progress.getReviewPriority());
            vo.setIsFavorite(progress.getIsFavorite());
            vo.setIsWrongQuestion(progress.getIsWrongQuestion());
            vo.setLastScore(progress.getLastScore());
            vo.setLastStudyTime(progress.getLastStudyTime());
            vo.setNextReviewTime(progress.getNextReviewTime());
        } else {
            vo.setStudyStatus(StudyConstants.STUDY_STATUS_NOT_STARTED);
            vo.setMasteryLevel(0);
            vo.setReviewPriority(3);
            vo.setIsFavorite(0);
            vo.setIsWrongQuestion(0);
        }
        return vo;
    }

    private void fillProgress(StudyQuestionDetailVO vo, StudyUserQuestionProgress progress) {
        if (progress == null) {
            vo.setStudyStatus(StudyConstants.STUDY_STATUS_NOT_STARTED);
            vo.setMasteryLevel(0);
            vo.setReviewPriority(3);
            vo.setIsFavorite(0);
            vo.setIsWrongQuestion(0);
            vo.setNoteCount(0);
            return;
        }
        vo.setStudyStatus(progress.getStudyStatus());
        vo.setMasteryLevel(progress.getMasteryLevel());
        vo.setReviewPriority(progress.getReviewPriority());
        vo.setIsFavorite(progress.getIsFavorite());
        vo.setIsWrongQuestion(progress.getIsWrongQuestion());
        vo.setNoteCount(progress.getNoteCount());
        vo.setLastScore(progress.getLastScore());
        vo.setBestScore(progress.getBestScore());
        vo.setAvgScore(progress.getAvgScore());
        vo.setLastStudyTime(progress.getLastStudyTime());
        vo.setLastCheckTime(progress.getLastCheckTime());
        vo.setNextReviewTime(progress.getNextReviewTime());
    }

    private StudyUserQuestionProgress getProgress(Long userId, Long questionId, boolean createIfAbsent) {
        if (userId == null) {
            if (createIfAbsent) {
                throw new BusinessException(401, "请先登录");
            }
            return null;
        }
        StudyUserQuestionProgress progress = progressMapper.selectOne(new LambdaQueryWrapper<StudyUserQuestionProgress>()
                .eq(StudyUserQuestionProgress::getUserId, userId)
                .eq(StudyUserQuestionProgress::getQuestionId, questionId)
                .last("LIMIT 1"));
        if (progress == null && createIfAbsent) {
            progress = new StudyUserQuestionProgress();
            progress.setUserId(userId);
            progress.setQuestionId(questionId);
            progress.setStudyStatus(StudyConstants.STUDY_STATUS_NOT_STARTED);
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
        return progress;
    }

    private void validateQuestionRequest(StudyQuestionSaveRequest request, Long id) {
        StudyCategory category = studyCategoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (request.getQuestionNo() != null) {
            LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<StudyQuestion>()
                    .eq(StudyQuestion::getCategoryId, request.getCategoryId())
                    .eq(StudyQuestion::getQuestionNo, request.getQuestionNo());
            if (id != null) {
                wrapper.ne(StudyQuestion::getId, id);
            }
            if (count(wrapper) > 0) {
                throw new BusinessException("同分类下题号已存在");
            }
        }
        if (request.getQuestionCode() != null && !request.getQuestionCode().trim().isEmpty()) {
            LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<StudyQuestion>()
                    .eq(StudyQuestion::getQuestionCode, request.getQuestionCode().trim());
            if (id != null) {
                wrapper.ne(StudyQuestion::getId, id);
            }
            if (count(wrapper) > 0) {
                throw new BusinessException("题目编码已存在");
            }
        }
    }

    private void fillQuestion(StudyQuestion question, StudyQuestionSaveRequest request, Long operatorId) {
        question.setCategoryId(request.getCategoryId());
        question.setQuestionNo(request.getQuestionNo());
        question.setQuestionCode(StringUtils.hasText(request.getQuestionCode())
                ? request.getQuestionCode().trim()
                : buildQuestionCode(request.getCategoryId(), request.getQuestionNo(), request.getTitle()));
        question.setQuestionType(request.getQuestionType() == null ? 1 : request.getQuestionType());
        question.setTitle(request.getTitle());
        question.setQuestionStem(request.getQuestionStem());
        question.setStandardAnswer(request.getStandardAnswer());
        question.setAnswerSummary(StringUtils.hasText(request.getAnswerSummary()) ? request.getAnswerSummary() : buildSummary(request.getStandardAnswer()));
        question.setKeywords(request.getKeywords());
        question.setDifficulty(request.getDifficulty() == null ? 2 : request.getDifficulty());
        question.setEstimatedMinutes(request.getEstimatedMinutes() == null ? estimateMinutes(request.getStandardAnswer()) : request.getEstimatedMinutes());
        question.setAnswerWordCount(countWords(request.getStandardAnswer()));
        question.setScoreFullMark(request.getScoreFullMark() == null ? new BigDecimal("100") : request.getScoreFullMark());
        question.setScorePassMark(request.getScorePassMark() == null ? new BigDecimal("60") : request.getScorePassMark());
        question.setAiScoreEnabled(request.getAiScoreEnabled() == null ? 1 : request.getAiScoreEnabled());
        question.setSelfAssessmentEnabled(request.getSelfAssessmentEnabled() == null ? 1 : request.getSelfAssessmentEnabled());
        question.setAiScorePromptVersion(StringUtils.hasText(request.getAiScorePromptVersion()) ? request.getAiScorePromptVersion() : "v1");
        question.setScoreRubricJson(request.getScoreRubricJson());
        question.setReviewStatus(request.getReviewStatus() == null ? 2 : request.getReviewStatus());
        question.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        question.setSourceType(1);
        question.setSourceFileName(request.getSourceFileName());
        question.setSourceSection(request.getSourceSection());
        if (StudyConstants.REVIEW_PUBLISHED.equals(question.getReviewStatus())) {
            if (question.getPublishTime() == null) {
                question.setPublishTime(LocalDateTime.now());
            }
        } else {
            question.setPublishTime(null);
        }
        question.setUpdateBy(operatorId);
    }

    private void insertVersion(StudyQuestion question, Integer changeType, String reason, Long operatorId) {
        StudyQuestionVersion version = new StudyQuestionVersion();
        version.setQuestionId(question.getId());
        version.setVersionNo(question.getVersionNo());
        version.setTitle(question.getTitle());
        version.setQuestionStem(question.getQuestionStem());
        version.setStandardAnswer(question.getStandardAnswer());
        version.setAnswerSummary(question.getAnswerSummary());
        version.setKeywords(question.getKeywords());
        version.setDifficulty(question.getDifficulty());
        version.setScoreFullMark(question.getScoreFullMark());
        version.setScorePassMark(question.getScorePassMark());
        version.setScoreRubricJson(question.getScoreRubricJson());
        version.setChangeType(changeType);
        version.setChangeReason(reason);
        version.setOperatorId(operatorId);
        questionVersionMapper.insert(version);
    }

    private String buildQuestionCode(Long categoryId, Integer questionNo, String title) {
        String prefix = categoryId == null ? "study" : "study-" + categoryId;
        if (questionNo != null) {
            return prefix + "-" + questionNo;
        }
        return prefix + "-" + Math.abs(title.hashCode());
    }

    private String buildSummary(String answer) {
        if (!StringUtils.hasText(answer)) {
            return null;
        }
        String value = answer.replace("\n", " ").trim();
        return value.length() <= 180 ? value : value.substring(0, 180) + "...";
    }

    private int countWords(String answer) {
        if (!StringUtils.hasText(answer)) {
            return 0;
        }
        return answer.replaceAll("\\s+", "").length();
    }

    private int estimateMinutes(String answer) {
        int length = countWords(answer);
        if (length > 1000) {
            return 10;
        }
        if (length > 500) {
            return 8;
        }
        return 5;
    }

    private int defaultReviewPriority(Integer masteryLevel) {
        if (masteryLevel == null) {
            return 3;
        }
        return Math.max(1, 6 - masteryLevel);
    }

    private void saveOrUpdateProgress(StudyUserQuestionProgress progress) {
        if (progress.getId() == null) {
            progressMapper.insert(progress);
        } else {
            progressMapper.updateById(progress);
        }
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

    private void clearAllStudyOverviewCacheAfterCommit() {
        redisService.runAfterCommit(redisService::clearAllStudyOverviewCache);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
