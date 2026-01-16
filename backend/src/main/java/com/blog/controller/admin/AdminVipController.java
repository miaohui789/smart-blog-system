package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.dto.request.VipKeyGenerateRequest;
import com.blog.dto.request.VipMemberUpdateRequest;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.entity.VipActivationKey;
import com.blog.entity.VipArticleHeat;
import com.blog.entity.VipMember;
import com.blog.mapper.VipArticleHeatMapper;
import com.blog.service.ArticleService;
import com.blog.service.UserService;
import com.blog.service.VipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "VIP管理接口")
@RestController
@RequestMapping("/api/admin/vip")
@RequiredArgsConstructor
public class AdminVipController {

    private final VipService vipService;
    private final VipArticleHeatMapper vipArticleHeatMapper;
    private final UserService userService;
    private final ArticleService articleService;

    // ========== 会员管理 ==========

    @Operation(summary = "会员列表")
    @GetMapping("/members")
    public Result<?> getMemberList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer status) {
        IPage<VipMember> pageResult = vipService.getMemberPage(page, size, keyword, level, status);
        return Result.success(pageResult);
    }

    @Operation(summary = "编辑会员")
    @PutMapping("/members/{id}")
    public Result<?> updateMember(@PathVariable Long id, @Validated @RequestBody VipMemberUpdateRequest request) {
        try {
            vipService.updateMember(id, request);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除会员")
    @DeleteMapping("/members/{id}")
    public Result<?> deleteMember(@PathVariable Long id) {
        try {
            vipService.deleteMember(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 密钥管理 ==========

    @Operation(summary = "密钥列表")
    @GetMapping("/keys")
    public Result<?> getKeyList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer status) {
        IPage<VipActivationKey> pageResult = vipService.getKeyPage(page, size, keyword, level, status);
        return Result.success(pageResult);
    }

    @Operation(summary = "批量生成密钥")
    @PostMapping("/keys/generate")
    public Result<?> generateKeys(@Validated @RequestBody VipKeyGenerateRequest request) {
        List<VipActivationKey> keys = vipService.generateKeys(request);
        return Result.success("生成成功", keys);
    }

    @Operation(summary = "更新密钥状态")
    @PutMapping("/keys/{id}/status")
    public Result<?> updateKeyStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            vipService.updateKeyStatus(id, status);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除密钥")
    @DeleteMapping("/keys/{id}")
    public Result<?> deleteKey(@PathVariable Long id) {
        vipService.deleteKey(id);
        return Result.success("删除成功");
    }

    // ========== 统计 ==========

    @Operation(summary = "VIP统计数据")
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        Map<String, Object> stats = vipService.getStatistics();
        return Result.success(stats);
    }

    // ========== 加热记录 ==========

    @Operation(summary = "加热记录列表")
    @GetMapping("/heats")
    public Result<?> getHeatList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long articleId) {
        
        LambdaQueryWrapper<VipArticleHeat> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(VipArticleHeat::getUserId, userId);
        }
        if (articleId != null) {
            wrapper.eq(VipArticleHeat::getArticleId, articleId);
        }
        wrapper.orderByDesc(VipArticleHeat::getCreateTime);
        
        Page<VipArticleHeat> pageResult = vipArticleHeatMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 获取用户和文章信息
        Set<Long> userIds = new HashSet<>();
        Set<Long> articleIds = new HashSet<>();
        pageResult.getRecords().forEach(h -> {
            userIds.add(h.getUserId());
            articleIds.add(h.getArticleId());
        });
        
        Map<Long, User> userMap = new HashMap<>();
        Map<Long, Article> articleMap = new HashMap<>();
        
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        if (!articleIds.isEmpty()) {
            articleService.listByIds(articleIds).forEach(a -> articleMap.put(a.getId(), a));
        }
        
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId());
            map.put("articleId", h.getArticleId());
            map.put("userId", h.getUserId());
            map.put("heatValue", h.getHeatValue());
            map.put("createTime", h.getCreateTime());
            
            User user = userMap.get(h.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                map.put("user", userInfo);
            }
            
            Article article = articleMap.get(h.getArticleId());
            if (article != null) {
                Map<String, Object> articleInfo = new HashMap<>();
                articleInfo.put("id", article.getId());
                articleInfo.put("title", article.getTitle());
                map.put("article", articleInfo);
            }
            
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    @Operation(summary = "加热统计")
    @GetMapping("/heats/stats")
    public Result<?> getHeatStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalHeats", vipArticleHeatMapper.selectCount(null));
        
        // 今日加热数
        LambdaQueryWrapper<VipArticleHeat> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.apply("DATE(create_time) = CURDATE()");
        data.put("todayHeats", vipArticleHeatMapper.selectCount(todayWrapper));
        
        // 总加热值
        LambdaQueryWrapper<VipArticleHeat> sumWrapper = new LambdaQueryWrapper<>();
        List<VipArticleHeat> allHeats = vipArticleHeatMapper.selectList(sumWrapper);
        int totalHeatValue = allHeats.stream().mapToInt(VipArticleHeat::getHeatValue).sum();
        data.put("totalHeatValue", totalHeatValue);
        
        return Result.success(data);
    }
}
