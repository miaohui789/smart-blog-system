package com.blog.dto.request;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class VipKeyGenerateRequest {
    @NotNull(message = "VIP等级不能为空")
    @Min(value = 1, message = "VIP等级最小为1")
    @Max(value = 3, message = "VIP等级最大为3")
    private Integer level;
    
    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数最小为1")
    private Integer durationDays;
    
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量最小为1")
    @Max(value = 100, message = "单次最多生成100个")
    private Integer count;
    
    private String remark;
    
    private Integer expireDays; // 密钥有效期（天），null表示永不过期
}
