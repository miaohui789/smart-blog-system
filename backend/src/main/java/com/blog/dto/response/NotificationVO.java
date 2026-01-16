package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String targetType;
    private Long targetId;
    private Integer isRead;
    private LocalDateTime createTime;
    
    /** 发送者信息 */
    private SenderInfo sender;
    
    /** 额外数据 */
    private Object extraData;
    
    @Data
    public static class SenderInfo {
        private Long id;
        private String nickname;
        private String avatar;
        private Integer vipLevel;
    }
}
