package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息VO
 */
@Data
public class MessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer type;
    private Integer isRead;
    private Boolean isWithdrawn;
    private LocalDateTime createTime;
    
    /** 发送者信息 */
    private UserInfo sender;
    
    /** 是否是自己发送的 */
    private Boolean isSelf;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
        private Integer vipLevel;
    }
}
