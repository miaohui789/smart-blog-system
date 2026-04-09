package com.blog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SearchArticleVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String summary;
    private String cover;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private LocalDateTime publishTime;
    private UserVO author;
    private List<TagVO> tags;
    private String titleHighlight;
    private String summaryHighlight;
}
