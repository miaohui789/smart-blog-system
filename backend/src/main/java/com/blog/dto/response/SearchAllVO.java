package com.blog.dto.response;

import com.blog.common.result.PageResult;
import lombok.Data;

import java.util.List;

@Data
public class SearchAllVO {

    /**
     * 当前搜索引擎来源：elasticsearch / database
     */
    private String engine;

    private PageResult<SearchArticleVO> articles;

    private PageResult<SearchStudyQuestionVO> studyQuestions;

    private List<UserVO> users;
}
