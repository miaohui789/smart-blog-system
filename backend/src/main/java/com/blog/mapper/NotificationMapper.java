package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
    
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND type = #{type} AND is_read = 0")
    int markTypeAsRead(@Param("userId") Long userId, @Param("type") String type);
}
