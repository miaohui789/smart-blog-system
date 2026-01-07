package com.blog.dto.request;

import lombok.Data;

@Data
public class ProfileRequest {
    private String nickname;
    private String email;
    private String intro;
    private String website;
}
