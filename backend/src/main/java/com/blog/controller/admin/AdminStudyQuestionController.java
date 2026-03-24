package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.request.StudyQuestionSaveRequest;
import com.blog.entity.StudyCategory;
import com.blog.entity.StudyQuestion;
import com.blog.service.StudyCategoryService;
import com.blog.service.StudyQuestionService;
import com.blog.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Tag(name = "学习题库管理")
@RestController
@RequestMapping("/api/admin/study/question")
@RequiredArgsConstructor
public class AdminStudyQuestionController {

    private final StudyQuestionService studyQuestionService;
    private final StudyCategoryService studyCategoryService;

    @Operation(summary = "题目分页列表")
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(StudyQuestion::getCategoryId, categoryId);
        }
        if (difficulty != null) {
            wrapper.eq(StudyQuestion::getDifficulty, difficulty);
        }
        if (reviewStatus != null) {
            wrapper.eq(StudyQuestion::getReviewStatus, reviewStatus);
        }
        if (status != null) {
            wrapper.eq(StudyQuestion::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(StudyQuestion::getTitle, keyword)
                    .or().like(StudyQuestion::getQuestionCode, keyword)
                    .or().like(StudyQuestion::getKeywords, keyword));
        }
        wrapper.orderByDesc(StudyQuestion::getUpdateTime).orderByDesc(StudyQuestion::getId);

        Page<StudyQuestion> pageResult = studyQuestionService.page(new Page<>(page, pageSize), wrapper);
        List<StudyQuestion> records = pageResult.getRecords();
        Set<Long> categoryIds = records.stream()
                .map(StudyQuestion::getCategoryId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : studyCategoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(StudyCategory::getId, StudyCategory::getCategoryName, (left, right) -> left));

        List<Map<String, Object>> list = records.stream().map(question -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", question.getId());
            item.put("categoryId", question.getCategoryId());
            item.put("categoryName", categoryNameMap.get(question.getCategoryId()));
            item.put("questionNo", question.getQuestionNo());
            item.put("questionCode", question.getQuestionCode());
            item.put("title", question.getTitle());
            item.put("difficulty", question.getDifficulty());
            item.put("estimatedMinutes", question.getEstimatedMinutes());
            item.put("scoreFullMark", question.getScoreFullMark());
            item.put("scorePassMark", question.getScorePassMark());
            item.put("aiScoreEnabled", question.getAiScoreEnabled());
            item.put("selfAssessmentEnabled", question.getSelfAssessmentEnabled());
            item.put("reviewStatus", question.getReviewStatus());
            item.put("status", question.getStatus());
            item.put("sourceFileName", question.getSourceFileName());
            item.put("sourceSection", question.getSourceSection());
            item.put("viewCount", question.getViewCount());
            item.put("studyCount", question.getStudyCount());
            item.put("checkCount", question.getCheckCount());
            item.put("versionNo", question.getVersionNo());
            item.put("updateTime", question.getUpdateTime());
            return item;
        }).collect(Collectors.toList());
        return Result.success(PageResult.of(list, pageResult.getTotal(), page, pageSize));
    }

    @Operation(summary = "题目详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        StudyQuestion question = studyQuestionService.getById(id);
        if (question == null) {
            return Result.success(null);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("question", question);
        StudyCategory category = question.getCategoryId() == null ? null : studyCategoryService.getById(question.getCategoryId());
        data.put("categoryName", category == null ? null : category.getCategoryName());
        return Result.success(data);
    }

    @Operation(summary = "新增题目")
    @PostMapping
    public Result<StudyQuestion> create(@Validated @RequestBody StudyQuestionSaveRequest request) {
        return Result.success(studyQuestionService.saveQuestion(request, SecurityUtils.requireCurrentUserId()));
    }

    @Operation(summary = "编辑题目")
    @PutMapping("/{id}")
    public Result<StudyQuestion> update(@PathVariable Long id, @Validated @RequestBody StudyQuestionSaveRequest request) {
        return Result.success(studyQuestionService.updateQuestion(id, request, SecurityUtils.requireCurrentUserId()));
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        studyQuestionService.deleteQuestion(id, SecurityUtils.requireCurrentUserId());
        return Result.success("删除成功", null);
    }

    @Operation(summary = "执行题库导入脚本")
    @PostMapping("/import")
    public Result<Map<String, Object>> importQuestions(@RequestBody(required = false) Map<String, Object> body) {
        Long operatorId = SecurityUtils.requireCurrentUserId();
        Map<String, Object> options = body == null ? Collections.emptyMap() : body;
        Path projectRoot = resolveProjectRoot();
        Path scriptPath = projectRoot.resolve("scripts").resolve("import_study_questions.py");
        if (!Files.exists(scriptPath)) {
            return Result.error("导入脚本不存在: " + scriptPath);
        }

        List<String> command = new ArrayList<>();
        command.add("python");
        command.add(scriptPath.toString());
        command.add("--operator-id");
        command.add(String.valueOf(operatorId));
        if (getBoolean(options, "bootstrap")) {
            command.add("--bootstrap");
        }
        if (getBoolean(options, "dryRun")) {
            command.add("--dry-run");
        }
        if (options.get("limit") != null) {
            command.add("--limit");
            command.add(String.valueOf(options.get("limit")));
        }
        if (StringUtils.hasText(asText(options.get("onlyFile")))) {
            command.add("--only-file");
            command.add(asText(options.get("onlyFile")));
        }
        if (StringUtils.hasText(asText(options.get("sourceDir")))) {
            command.add("--source-dir");
            command.add(asText(options.get("sourceDir")));
        }

        try {
            Process process = new ProcessBuilder(command)
                    .directory(projectRoot.toFile())
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(10, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                return Result.error("导入超时，请稍后重试");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("command", String.join(" ", command));
            result.put("exitCode", process.exitValue());
            result.put("success", process.exitValue() == 0);
            result.put("output", output);
            if (process.exitValue() != 0) {
                return Result.error("导入执行失败，详情请查看 output");
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入执行异常: " + e.getMessage());
        }
    }

    private Path resolveProjectRoot() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if ("backend".equalsIgnoreCase(current.getFileName().toString()) && current.getParent() != null) {
            return current.getParent();
        }
        return current;
    }

    private boolean getBoolean(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }

    private String asText(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
