package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 接收通知的用户ID */
    private Long userId;
    
    /** 通知类型：LIKE_ARTICLE/FAVORITE_ARTICLE/COMMENT/REPLY/FOLLOW/MESSAGE/SYSTEM */
    private String type;
    
    /** 通知标题 */
    private String title;
    
    /** 通知内容 */
    private String content;
    
    /** 触发通知的用户ID */
    private Long senderId;
    
    /** 目标类型：article/comment */
    private String targetType;
    
    /** 目标ID */
    private Long targetId;
    
    /** 额外数据JSON */
    private String extraData;
    
    /** 是否已读 */
    private Integer isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer isDeleted;
}
