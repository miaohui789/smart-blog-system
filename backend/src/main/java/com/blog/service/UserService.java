package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    User getByUsername(String username);
    
    User getByEmail(String email);
    
    User getByUsernameOrEmail(String usernameOrEmail);
    
    /**
     * 搜索用户
     * @param keyword 关键词（匹配用户名或昵称）
     * @param limit 返回数量限制
     * @return 用户列表
     */
    List<User> searchUsers(String keyword, int limit);
}
