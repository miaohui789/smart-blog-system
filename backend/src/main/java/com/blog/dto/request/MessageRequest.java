package com.blog.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发送消息请求
 */
@Data
public class MessageRequest {
    
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过500字")
    private String content;
    
    /** 消息类型：1文本 2图片 */
    private Integer type;
}
