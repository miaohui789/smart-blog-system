package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String intro;
    private String website;
    private String themeMode;
    private String darkSkin;
    private String lightSkin;
    private Integer status;
    private String loginIp;
    private LocalDateTime loginTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
    
    // VIP相关字段
    private Integer isVip;
    private Integer vipLevel;
    private LocalDateTime vipExpireTime;
    private Integer userLevel;
    private Integer currentExp;
    private Integer totalExp;
    private LocalDateTime expUpdateTime;
    
    // 社交统计字段
    private Integer followCount;
    private Integer fansCount;
    private Integer articleCount;
}
