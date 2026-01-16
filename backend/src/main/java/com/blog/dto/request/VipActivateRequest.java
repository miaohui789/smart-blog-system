package com.blog.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class VipActivateRequest {
    @NotBlank(message = "激活密钥不能为空")
    @Pattern(regexp = "^VIP-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$", message = "密钥格式不正确")
    private String keyCode;
}
