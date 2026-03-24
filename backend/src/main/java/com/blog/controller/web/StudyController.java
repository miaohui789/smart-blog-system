package com.blog.controller.web;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.StudyAnswerSubmitRequest;
import com.blog.dto.request.StudyQuestionNoteRequest;
import com.blog.dto.request.StudyQuestionStatusRequest;
import com.blog.dto.request.StudyTaskCreateRequest;
import com.blog.dto.response.StudyCategoryVO;
import com.blog.dto.response.StudyCheckHistoryVO;
import com.blog.dto.response.StudyCheckStatisticsVO;
import com.blog.dto.response.StudyCheckTaskItemVO;
import com.blog.dto.response.StudyCheckTaskVO;
import com.blog.dto.response.StudyDashboardVO;
import com.blog.dto.response.StudyNoteVO;
import com.blog.dto.response.StudyProgressOverviewVO;
import com.blog.dto.response.StudyQuestionDetailVO;
import com.blog.dto.response.StudyQuestionListVO;
import com.blog.service.StudyCategoryService;
import com.blog.service.StudyCheckTaskService;
import com.blog.service.StudyDashboardService;
import com.blog.service.StudyQuestionService;
import com.blog.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "学习模块接口")
@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyCategoryService studyCategoryService;
    private final StudyQuestionService studyQuestionService;
    private final StudyCheckTaskService studyCheckTaskService;
    private final StudyDashboardService studyDashboardService;

    @Operation(summary = "获取学习分类树")
    @GetMapping("/categories")
    public Result<List<StudyCategoryVO>> getCategories() {
        return Result.success(studyCategoryService.getCategoryTree(false));
    }

    @Operation(summary = "分页获取题目列表")
    @GetMapping("/questions")
    public Result<PageResult<StudyQuestionListVO>> getQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer studyStatus,
            @RequestParam(required = false) Integer isFavorite,
            @RequestParam(required = false) String studyCountSort) {
        return Result.success(studyQuestionService.getQuestionPage(
                SecurityUtils.getCurrentUserId(), page, pageSize, categoryId, keyword, difficulty, studyStatus, isFavorite, studyCountSort));
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/questions/{id}")
    public Result<StudyQuestionDetailVO> getQuestionDetail(@PathVariable Long id) {
        return Result.success(studyQuestionService.getQuestionDetail(SecurityUtils.getCurrentUserId(), id));
    }

    @Operation(summary = "随机获取一道题")
    @GetMapping("/questions/random")
    public Result<StudyQuestionDetailVO> getRandomQuestion(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Boolean preferPendingReview,
            @RequestParam(required = false) Boolean preferLowStudyCount,
            @RequestParam(required = false) String excludeIds) {
        return Result.success(studyQuestionService.getRandomQuestion(
                SecurityUtils.getCurrentUserId(), categoryId, difficulty, preferPendingReview, preferLowStudyCount, excludeIds));
    }

    @Operation(summary = "获取学习总览")
    @GetMapping("/progress/overview")
    public Result<StudyProgressOverviewVO> getOverview() {
        return Result.success(studyQuestionService.getOverview(SecurityUtils.requireCurrentUserId()));
    }

    @Operation(summary = "获取我的学习中心数据")
    @GetMapping("/dashboard")
    public Result<StudyDashboardVO> getDashboard() {
        return Result.success(studyDashboardService.getUserDashboard(SecurityUtils.requireCurrentUserId()));
    }

    @Operation(summary = "记录学习一次")
    @PostMapping("/questions/{id}/study")
    public Result<?> recordStudy(@PathVariable Long id) {
        studyQuestionService.recordStudy(SecurityUtils.requireCurrentUserId(), id);
        return Result.success("记录成功", null);
    }

    @Operation(summary = "收藏或取消收藏题目")
    @PostMapping("/questions/{id}/favorite")
    public Result<?> toggleFavorite(@PathVariable Long id, @RequestParam(required = false) Integer favorite) {
        studyQuestionService.toggleFavorite(SecurityUtils.requireCurrentUserId(), id, favorite);
        return Result.success("操作成功", null);
    }

    @Operation(summary = "保存学习笔记")
    @PostMapping("/questions/{id}/note")
    public Result<StudyNoteVO> saveNote(@PathVariable Long id, @Validated @RequestBody StudyQuestionNoteRequest request) {
        return Result.success(studyQuestionService.saveNote(SecurityUtils.requireCurrentUserId(), id, request));
    }

    @Operation(summary = "更新学习状态")
    @PostMapping("/questions/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @Validated @RequestBody StudyQuestionStatusRequest request) {
        studyQuestionService.updateProgressStatus(SecurityUtils.requireCurrentUserId(), id, request);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "创建抽查任务")
    @PostMapping("/check/tasks")
    public Result<StudyCheckTaskVO> createTask(@Validated @RequestBody StudyTaskCreateRequest request) {
        return Result.success(studyCheckTaskService.createTask(SecurityUtils.requireCurrentUserId(), request));
    }

    @Operation(summary = "获取抽查任务详情")
    @GetMapping("/check/tasks/{taskId}")
    public Result<StudyCheckTaskVO> getTaskDetail(@PathVariable Long taskId) {
        return Result.success(studyCheckTaskService.getTaskDetail(SecurityUtils.requireCurrentUserId(), taskId, false));
    }

    @Operation(summary = "标记已查看标准答案")
    @PostMapping("/check/tasks/{taskId}/items/{itemId}/view-answer")
    public Result<?> markViewAnswer(@PathVariable Long taskId, @PathVariable Long itemId) {
        studyCheckTaskService.markViewAnswer(SecurityUtils.requireCurrentUserId(), taskId, itemId);
        return Result.success("标记成功", null);
    }

    @Operation(summary = "提交抽查答案")
    @PostMapping("/check/tasks/{taskId}/items/{itemId}/submit")
    public Result<StudyCheckTaskItemVO> submitAnswer(@PathVariable Long taskId,
                                                     @PathVariable Long itemId,
                                                     @Validated @RequestBody StudyAnswerSubmitRequest request) {
        return Result.success(studyCheckTaskService.submitAnswer(SecurityUtils.requireCurrentUserId(), taskId, itemId, request));
    }

    @Operation(summary = "完成抽查任务")
    @PostMapping("/check/tasks/{taskId}/finish")
    public Result<StudyCheckTaskVO> finishTask(@PathVariable Long taskId) {
        return Result.success(studyCheckTaskService.finishTask(SecurityUtils.requireCurrentUserId(), taskId));
    }

    @Operation(summary = "获取抽查历史")
    @GetMapping("/check/history")
    public Result<PageResult<StudyCheckHistoryVO>> getHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(studyCheckTaskService.getHistory(SecurityUtils.requireCurrentUserId(), page, pageSize, status));
    }

    @Operation(summary = "获取抽查统计")
    @GetMapping("/check/statistics")
    public Result<StudyCheckStatisticsVO> getStatistics() {
        return Result.success(studyCheckTaskService.getStatistics(SecurityUtils.requireCurrentUserId()));
    }
}
