package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户详情页VO
 */
@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String intro;
    private String website;
    private Integer vipLevel;
    private LocalDateTime vipExpireTime;
    
    /** 用户状态：0禁用 1正常 */
    private Integer status;
    
    /** 关注数 */
    private Long followCount;
    
    /** 粉丝数 */
    private Long fansCount;
    
    /** 文章数 */
    private Long articleCount;
    
    /** 当前用户是否已关注此用户 */
    private Boolean isFollowed;
    
    /** 注册时间 */
    private LocalDateTime createTime;
}
