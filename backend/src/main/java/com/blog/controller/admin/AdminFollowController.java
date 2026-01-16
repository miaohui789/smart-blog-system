package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.entity.User;
import com.blog.entity.UserFollow;
import com.blog.service.UserFollowService;
import com.blog.service.UserService;
import com.blog.mapper.UserFollowMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "关注管理")
@RestController
@RequestMapping("/api/admin/follows")
@RequiredArgsConstructor
public class AdminFollowController {

    private final UserFollowService userFollowService;
    private final UserFollowMapper userFollowMapper;
    private final UserService userService;

    @Operation(summary = "关注关系列表")
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long followUserId) {
        
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(UserFollow::getUserId, userId);
        }
        
        if (followUserId != null) {
            wrapper.eq(UserFollow::getFollowUserId, followUserId);
        }
        
        wrapper.orderByDesc(UserFollow::getCreateTime);
        
        Page<UserFollow> pageResult = userFollowMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        // 获取用户信息
        Set<Long> userIds = new HashSet<>();
        pageResult.getRecords().forEach(f -> {
            userIds.add(f.getUserId());
            userIds.add(f.getFollowUserId());
        });
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("userId", f.getUserId());
            map.put("followUserId", f.getFollowUserId());
            map.put("createTime", f.getCreateTime());
            
            User user = userMap.get(f.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("username", user.getUsername());
                map.put("user", userInfo);
            }
            
            User followUser = userMap.get(f.getFollowUserId());
            if (followUser != null) {
                Map<String, Object> followUserInfo = new HashMap<>();
                followUserInfo.put("id", followUser.getId());
                followUserInfo.put("nickname", followUser.getNickname());
                followUserInfo.put("avatar", followUser.getAvatar());
                followUserInfo.put("username", followUser.getUsername());
                map.put("followUser", followUserInfo);
            }
            
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    @Operation(summary = "用户关注统计")
    @GetMapping("/user/{userId}/stats")
    public Result<?> userStats(@PathVariable Long userId) {
        Map<String, Object> data = new HashMap<>();
        
        // 关注数
        LambdaQueryWrapper<UserFollow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(UserFollow::getUserId, userId);
        data.put("followingCount", userFollowMapper.selectCount(followingWrapper));
        
        // 粉丝数
        LambdaQueryWrapper<UserFollow> followerWrapper = new LambdaQueryWrapper<>();
        followerWrapper.eq(UserFollow::getFollowUserId, userId);
        data.put("followerCount", userFollowMapper.selectCount(followerWrapper));
        
        return Result.success(data);
    }

    @Operation(summary = "删除关注关系")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userFollowMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "关注统计")
    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalFollows", userFollowMapper.selectCount(null));
        
        // 今日新增关注
        LambdaQueryWrapper<UserFollow> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.apply("DATE(create_time) = CURDATE()");
        data.put("todayFollows", userFollowMapper.selectCount(todayWrapper));
        
        // 活跃用户数（有关注行为的用户）
        LambdaQueryWrapper<UserFollow> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.select(UserFollow::getUserId);
        activeWrapper.groupBy(UserFollow::getUserId);
        data.put("activeUsers", userFollowMapper.selectCount(activeWrapper));
        
        // 互相关注数（简化计算）
        data.put("mutualFollows", 0);
        
        // 获取粉丝最多的用户Top10
        List<Map<String, Object>> topUsers = new ArrayList<>();
        List<User> allUsers = userService.list();
        allUsers.stream()
            .filter(u -> u.getFansCount() != null && u.getFansCount() > 0)
            .sorted((a, b) -> b.getFansCount().compareTo(a.getFansCount()))
            .limit(10)
            .forEach(u -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", u.getId());
                userMap.put("nickname", u.getNickname());
                userMap.put("username", u.getUsername());
                userMap.put("avatar", u.getAvatar());
                userMap.put("fansCount", u.getFansCount());
                topUsers.add(userMap);
            });
        data.put("topUsers", topUsers);
        
        return Result.success(data);
    }

    @Operation(summary = "批量删除关注关系")
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的关注关系");
        }
        userFollowMapper.deleteBatchIds(ids);
        return Result.success("删除成功");
    }

    @Operation(summary = "用户关注列表")
    @GetMapping("/user/{userId}/following")
    public Result<?> userFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId);
        wrapper.orderByDesc(UserFollow::getCreateTime);
        
        Page<UserFollow> pageResult = userFollowMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        Set<Long> followUserIds = pageResult.getRecords().stream()
            .map(UserFollow::getFollowUserId)
            .collect(Collectors.toSet());
        
        Map<Long, User> userMap = new HashMap<>();
        if (!followUserIds.isEmpty()) {
            userService.listByIds(followUserIds).forEach(u -> userMap.put(u.getId(), u));
        }
        
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("createTime", f.getCreateTime());
            
            User followUser = userMap.get(f.getFollowUserId());
            if (followUser != null) {
                map.put("userId", followUser.getId());
                map.put("nickname", followUser.getNickname());
                map.put("avatar", followUser.getAvatar());
                map.put("username", followUser.getUsername());
            }
            
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }

    @Operation(summary = "用户粉丝列表")
    @GetMapping("/user/{userId}/followers")
    public Result<?> userFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowUserId, userId);
        wrapper.orderByDesc(UserFollow::getCreateTime);
        
        Page<UserFollow> pageResult = userFollowMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        Set<Long> followerIds = pageResult.getRecords().stream()
            .map(UserFollow::getUserId)
            .collect(Collectors.toSet());
        
        Map<Long, User> userMap = new HashMap<>();
        if (!followerIds.isEmpty()) {
            userService.listByIds(followerIds).forEach(u -> userMap.put(u.getId(), u));
        }
        
        List<Map<String, Object>> list = pageResult.getRecords().stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("createTime", f.getCreateTime());
            
            User follower = userMap.get(f.getUserId());
            if (follower != null) {
                map.put("userId", follower.getId());
                map.put("nickname", follower.getNickname());
                map.put("avatar", follower.getAvatar());
                map.put("username", follower.getUsername());
            }
            
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotal());
        return Result.success(result);
    }
}
