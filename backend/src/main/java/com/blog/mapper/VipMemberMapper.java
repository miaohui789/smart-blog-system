package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.VipMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VipMemberMapper extends BaseMapper<VipMember> {
    
    @Select("<script>" +
            "SELECT vm.*, u.username, u.nickname, u.avatar " +
            "FROM vip_member vm " +
            "LEFT JOIN sys_user u ON vm.user_id = u.id " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test='level != null'>" +
            "    AND vm.level = #{level}" +
            "  </if>" +
            "  <if test='status != null'>" +
            "    AND vm.status = #{status}" +
            "  </if>" +
            "</where>" +
            "ORDER BY vm.create_time DESC" +
            "</script>")
    IPage<VipMember> selectMemberPage(Page<VipMember> page, @Param("keyword") String keyword, 
                                       @Param("level") Integer level, @Param("status") Integer status);
}
