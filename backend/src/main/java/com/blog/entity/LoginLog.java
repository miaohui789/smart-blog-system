package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class LoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String ip;
    private String ipSource;
    private String browser;
    private String os;
    private Integer status;
    private String message;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
