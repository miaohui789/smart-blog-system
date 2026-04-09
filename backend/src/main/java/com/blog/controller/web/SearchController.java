package com.blog.controller.web;

import com.blog.common.result.Result;
import com.blog.dto.response.HotSearchBoardVO;
import com.blog.dto.response.SearchAllVO;
import com.blog.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "全站搜索接口")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "全站搜索")
    @GetMapping
    public Result<SearchAllVO> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean recordHot) {
        return Result.success(searchService.search(keyword, page, pageSize, Boolean.TRUE.equals(recordHot)));
    }

    @Operation(summary = "获取热搜榜单")
    @GetMapping("/hot")
    public Result<HotSearchBoardVO> hot(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(searchService.getHotSearchBoard(limit));
    }
}
