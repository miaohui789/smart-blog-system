package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_import_batch_detail")
public class StudyImportBatchDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long batchId;

    private Integer sourceLineNo;

    private String sourceSection;

    private String sourceQuestionNo;

    private String sourceTitle;

    private String categoryName;

    private Long questionId;

    private Integer processStatus;

    private String errorMessage;

    private String rawPayload;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
