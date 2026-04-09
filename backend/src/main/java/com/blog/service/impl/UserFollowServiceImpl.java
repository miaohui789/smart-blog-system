package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.constant.ExpConstants;
import com.blog.entity.User;
import com.blog.entity.UserFollow;
import com.blog.mapper.UserFollowMapper;
import com.blog.mapper.UserMapper;
import com.blog.mq.UserExpAsyncService;
import com.blog.service.RedisService;
import com.blog.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    private final UserMapper userMapper;
    private final UserExpAsyncService userExpAsyncService;
    private final RedisService redisService;

    @Override
    @Transactional
    public boolean follow(Long userId, Long followUserId) {
        // 不能关注自己
        if (userId.equals(followUserId)) {
            return false;
        }
        
        // 检查是否已关注
        if (isFollowed(userId, followUserId)) {
            return false;
        }
        
        // 创建关注记录
        UserFollow follow = new UserFollow();
        follow.setUserId(userId);
        follow.setFollowUserId(followUserId);
        boolean success = save(follow);
        
        if (success) {
            // 更新关注者的关注数
            userMapper.updateFollowCount(userId, 1);
            // 更新被关注者的粉丝数
            userMapper.updateFansCount(followUserId, 1);
            redisService.runAfterCommit(() -> userExpAsyncService.publishGrant(
                    userId,
                    ExpConstants.BIZ_USER_FOLLOW,
                    "follow:" + userId + ":" + followUserId + ":" + System.currentTimeMillis(),
                    ExpConstants.EXP_USER_FOLLOW,
                    "关注用户获得经验",
                    "UserFollowService.follow"
            ));
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean unfollow(Long userId, Long followUserId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowUserId, followUserId);
        
        boolean success = remove(wrapper);
        
        if (success) {
            // 更新关注者的关注数
            userMapper.updateFollowCount(userId, -1);
            // 更新被关注者的粉丝数
            userMapper.updateFansCount(followUserId, -1);
        }
        
        return success;
    }

    @Override
    public boolean isFollowed(Long userId, Long followUserId) {
        return count(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId)) > 0;
    }

    @Override
    public long getFollowCount(Long userId) {
        return count(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId));
    }

    @Override
    public long getFansCount(Long userId) {
        return count(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowUserId, userId));
    }
}
