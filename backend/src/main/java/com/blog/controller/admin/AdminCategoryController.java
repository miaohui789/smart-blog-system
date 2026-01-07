package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<?> list() {
        return Result.success(categoryService.list());
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<?> create(@RequestBody Category category) {
        category.setArticleCount(0);
        category.setStatus(1);
        categoryService.save(category);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success("删除成功");
    }
}
