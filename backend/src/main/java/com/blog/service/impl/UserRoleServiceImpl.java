package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.UserRole;
import com.blog.mapper.UserRoleMapper;
import com.blog.service.UserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        List<Map<String, Object>> roles = baseMapper.selectRolesByUserId(userId);
        return roles.stream()
                .map(r -> (String) r.get("role_name"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getRolesWithStatusByUserId(Long userId) {
        return baseMapper.selectRolesWithStatusByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        // 先删除用户原有角色
        remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        
        // 添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoles = roleIds.stream().map(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                return userRole;
            }).collect(Collectors.toList());
            saveBatch(userRoles);
        }
    }
}
