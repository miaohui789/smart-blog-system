package com.blog.dto.request;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class VipMemberUpdateRequest {
    @NotNull(message = "VIP等级不能为空")
    @Min(value = 1, message = "VIP等级最小为1")
    @Max(value = 3, message = "VIP等级最大为3")
    private Integer level;
    
    @NotNull(message = "到期时间不能为空")
    private LocalDateTime expireTime;
}
