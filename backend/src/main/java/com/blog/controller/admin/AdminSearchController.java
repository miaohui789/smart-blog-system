package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "搜索索引管理")
@RestController
@RequestMapping("/api/admin/search")
@RequiredArgsConstructor
public class AdminSearchController {

    private final SearchService searchService;

    @Operation(summary = "获取搜索状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        return Result.success(searchService.getSearchStatus());
    }

    @Operation(summary = "重建搜索索引")
    @PostMapping("/rebuild")
    public Result<Map<String, Object>> rebuild(@RequestParam(defaultValue = "all") String scope) {
        return Result.success(searchService.rebuildIndex(scope));
    }
}
