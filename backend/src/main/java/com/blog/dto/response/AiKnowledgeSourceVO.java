package com.blog.dto.response;

import lombok.Data;

@Data
public class AiKnowledgeSourceVO {

    /**
     * article / study_question
     */
    private String sourceType;

    private Long sourceId;

    private String title;

    private String summary;

    private String categoryName;

    private String routePath;

    private String publishTime;

    private String keywords;

    private Integer difficulty;

    private String difficultyLabel;

    private Integer studyStatus;

    private String studyStatusLabel;

    private Integer isFavorite;

    private Integer isWrongQuestion;

    private Integer viewCount;

    private Integer studyCount;

    private Integer likeCount;

    private Integer favoriteCount;
}
