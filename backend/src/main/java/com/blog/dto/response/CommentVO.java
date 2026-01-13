package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private Long replyUserId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    
    // 用户信息
    private UserInfo user;
    
    // 回复的用户信息
    private UserInfo replyUser;
    
    // 子评论列表
    private List<CommentVO> replies;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
        private Integer vipLevel;
    }
}
