package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.UserRole;

import java.util.List;
import java.util.Map;

public interface UserRoleService extends IService<UserRole> {
    
    List<String> getRoleNamesByUserId(Long userId);
    
    List<Long> getRoleIdsByUserId(Long userId);
    
    void updateUserRoles(Long userId, List<Long> roleIds);
    
    /**
     * 获取用户角色详细信息（包含角色状态）
     */
    List<Map<String, Object>> getRolesWithStatusByUserId(Long userId);
}
