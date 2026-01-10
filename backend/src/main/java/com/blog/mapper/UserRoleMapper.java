package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    @Select("SELECT r.id, r.role_name FROM sys_user_role ur " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<java.util.Map<String, Object>> selectRolesByUserId(@Param("userId") Long userId);
    
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户角色详细信息（包含角色状态）
     */
    @Select("SELECT r.id, r.role_name, r.role_key, r.status FROM sys_user_role ur " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<java.util.Map<String, Object>> selectRolesWithStatusByUserId(@Param("userId") Long userId);
}
