package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.VipActivationKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VipActivationKeyMapper extends BaseMapper<VipActivationKey> {
    
    @Select("<script>" +
            "SELECT vak.*, u.username as used_by_username " +
            "FROM vip_activation_key vak " +
            "LEFT JOIN sys_user u ON vak.used_by = u.id " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (vak.key_code LIKE CONCAT('%', #{keyword}, '%') OR vak.remark LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test='level != null'>" +
            "    AND vak.level = #{level}" +
            "  </if>" +
            "  <if test='status != null'>" +
            "    AND vak.status = #{status}" +
            "  </if>" +
            "</where>" +
            "ORDER BY vak.create_time DESC" +
            "</script>")
    IPage<VipActivationKey> selectKeyPage(Page<VipActivationKey> page, @Param("keyword") String keyword,
                                           @Param("level") Integer level, @Param("status") Integer status);
}
