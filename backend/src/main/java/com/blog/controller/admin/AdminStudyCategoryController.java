package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.dto.request.StudyCategorySaveRequest;
import com.blog.dto.response.StudyCategoryVO;
import com.blog.entity.StudyCategory;
import com.blog.service.StudyCategoryService;
import com.blog.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "学习分类管理")
@RestController
@RequestMapping("/api/admin/study/category")
@RequiredArgsConstructor
public class AdminStudyCategoryController {

    private final StudyCategoryService studyCategoryService;

    @Operation(summary = "分类列表")
    @GetMapping("/list")
    public Result<List<StudyCategoryVO>> list(@RequestParam(defaultValue = "true") Boolean includeDisabled) {
        return Result.success(studyCategoryService.getCategoryTree(Boolean.TRUE.equals(includeDisabled)));
    }

    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    public Result<StudyCategory> detail(@PathVariable Long id) {
        return Result.success(studyCategoryService.getById(id));
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<StudyCategory> create(@Validated @RequestBody StudyCategorySaveRequest request) {
        return Result.success(studyCategoryService.saveCategory(request, SecurityUtils.requireCurrentUserId()));
    }

    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    public Result<StudyCategory> update(@PathVariable Long id, @Validated @RequestBody StudyCategorySaveRequest request) {
        return Result.success(studyCategoryService.updateCategory(id, request, SecurityUtils.requireCurrentUserId()));
    }
}
