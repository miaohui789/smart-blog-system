package com.blog.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String cover;
    private String summary;
    private String content;
    private String contentHtml;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer isTop;
    private Integer isRecommend;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private Long categoryId;
    private String categoryName;
    private UserVO author;
    private List<TagVO> tags;
    private Boolean isLiked;
    private Boolean isFavorited;
}
