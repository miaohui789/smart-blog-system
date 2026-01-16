package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 私信消息实体
 */
@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 会话ID */
    private Long conversationId;
    
    /** 发送者ID */
    private Long senderId;
    
    /** 接收者ID */
    private Long receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 消息类型：1文本 2图片 3系统消息 */
    private Integer type;
    
    /** 是否已读：0未读 1已读 */
    private Integer isRead;
    
    /** 是否已撤回：0否 1是 */
    private Integer isWithdrawn;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer isDeleted;
}
