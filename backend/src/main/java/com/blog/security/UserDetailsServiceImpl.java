package com.blog.security;

import com.blog.entity.User;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 支持用户名或邮箱登录
        User user = userService.getByUsernameOrEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new SecurityUser(user);
    }
}
