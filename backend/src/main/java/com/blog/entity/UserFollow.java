package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户关注实体
 */
@Data
@TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关注者ID（谁关注） */
    private Long userId;
    
    /** 被关注者ID（关注谁） */
    private Long followUserId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
