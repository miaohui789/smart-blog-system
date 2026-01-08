package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理")
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @Operation(summary = "标签列表")
    @GetMapping
    public Result<?> list() {
        return Result.success(tagService.list());
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<?> create(@RequestBody Tag tag) {
        tag.setArticleCount(0);
        tagService.save(tag);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        tagService.updateById(tag);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除标签")
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids != null && !ids.isEmpty()) {
            tagService.removeByIds(ids);
        }
        return Result.success("删除成功");
    }
}
