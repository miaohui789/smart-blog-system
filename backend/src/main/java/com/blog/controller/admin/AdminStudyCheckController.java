package com.blog.controller.admin;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.dto.response.StudyCheckHistoryVO;
import com.blog.dto.response.StudyCheckTaskVO;
import com.blog.service.StudyCheckTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "学习抽查记录管理")
@RestController
@RequestMapping("/api/admin/study/check-record")
@RequiredArgsConstructor
public class AdminStudyCheckController {

    private final StudyCheckTaskService studyCheckTaskService;

    @Operation(summary = "抽查记录分页列表")
    @GetMapping("/list")
    public Result<PageResult<StudyCheckHistoryVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(studyCheckTaskService.getAdminTaskPage(page, pageSize, userId, categoryId, status));
    }

    @Operation(summary = "抽查记录详情")
    @GetMapping("/{id}")
    public Result<StudyCheckTaskVO> detail(@PathVariable Long id) {
        return Result.success(studyCheckTaskService.getTaskDetail(null, id, true));
    }
}
