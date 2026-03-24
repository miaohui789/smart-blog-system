package com.blog.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudyDashboardFailedRecordVO {

    private Long taskId;
    private Long taskItemId;
    private Long questionId;
    private String taskName;
    private Integer checkMode;
    private String categoryName;
    private String questionTitle;
    private BigDecimal finalScore;
    private BigDecimal scorePassMark;
    private Integer selfAssessmentResult;
    private LocalDateTime submitTime;
}
