package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 更新用户关注数
     */
    @Update("UPDATE sys_user SET follow_count = GREATEST(0, COALESCE(follow_count, 0) + #{delta}) WHERE id = #{userId}")
    int updateFollowCount(@Param("userId") Long userId, @Param("delta") int delta);
    
    /**
     * 更新用户粉丝数
     */
    @Update("UPDATE sys_user SET fans_count = GREATEST(0, COALESCE(fans_count, 0) + #{delta}) WHERE id = #{userId}")
    int updateFansCount(@Param("userId") Long userId, @Param("delta") int delta);
    
    /**
     * 更新用户文章数
     */
    @Update("UPDATE sys_user SET article_count = GREATEST(0, COALESCE(article_count, 0) + #{delta}) WHERE id = #{userId}")
    int updateArticleCount(@Param("userId") Long userId, @Param("delta") int delta);
}
