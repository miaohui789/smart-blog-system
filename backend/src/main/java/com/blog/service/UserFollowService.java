package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.UserFollow;

public interface UserFollowService extends IService<UserFollow> {
    
    /**
     * 关注用户
     */
    boolean follow(Long userId, Long followUserId);
    
    /**
     * 取消关注
     */
    boolean unfollow(Long userId, Long followUserId);
    
    /**
     * 是否已关注
     */
    boolean isFollowed(Long userId, Long followUserId);
    
    /**
     * 获取关注数
     */
    long getFollowCount(Long userId);
    
    /**
     * 获取粉丝数
     */
    long getFansCount(Long userId);
}
