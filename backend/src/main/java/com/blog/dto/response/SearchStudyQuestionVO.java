package com.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchStudyQuestionVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private Integer questionNo;
    private String questionCode;
    private String title;
    private String answerSummary;
    private String keywords;
    private Integer difficulty;
    private Integer viewCount;
    private Integer studyCount;
    private LocalDateTime publishTime;
    private String titleHighlight;
    private String answerSummaryHighlight;
}
