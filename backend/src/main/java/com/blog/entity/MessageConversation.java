package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 私信会话实体
 */
@Data
@TableName("message_conversation")
public class MessageConversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户1 ID（较小的ID） */
    private Long user1Id;
    
    /** 用户2 ID（较大的ID） */
    private Long user2Id;
    
    /** 最后一条消息ID */
    private Long lastMessageId;
    
    /** 最后消息时间 */
    private LocalDateTime lastMessageTime;
    
    /** 用户1未读数 */
    private Integer user1Unread;
    
    /** 用户2未读数 */
    private Integer user2Unread;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
