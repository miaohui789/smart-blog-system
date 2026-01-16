package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String intro;
    private String website;
    private Integer status;
    private Integer vipLevel;
    private LocalDateTime createTime;
}
