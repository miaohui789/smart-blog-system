package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话列表VO
 */
@Data
public class ConversationVO {
    private Long id;
    
    /** 对方用户信息 */
    private UserInfo targetUser;
    
    /** 最后一条消息内容 */
    private String lastMessage;
    
    /** 最后消息时间 */
    private LocalDateTime lastMessageTime;
    
    /** 未读消息数 */
    private Integer unreadCount;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
        private Integer vipLevel;
    }
}
