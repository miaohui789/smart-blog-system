package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
    
    @Override
    public List<User> searchUsers(String keyword, int limit) {
        return list(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)  // 只搜索正常状态的用户
                .and(w -> w
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getNickname, keyword)
                )
                .select(User::getId, User::getUsername, User::getNickname, User::getAvatar, User::getIntro, User::getVipLevel)
                .last("LIMIT " + limit)
        );
    }
}
