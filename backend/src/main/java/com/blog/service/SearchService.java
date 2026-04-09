package com.blog.service;

import com.blog.dto.response.SearchAllVO;
import com.blog.dto.response.HotSearchBoardVO;

import java.util.Map;

public interface SearchService {

    SearchAllVO search(String keyword, Integer page, Integer pageSize);

    SearchAllVO search(String keyword, Integer page, Integer pageSize, boolean recordHot);

    HotSearchBoardVO getHotSearchBoard(Integer limit);

    void syncArticle(Long articleId);

    void deleteArticle(Long articleId);

    void syncUser(Long userId);

    void deleteUser(Long userId);

    void syncArticlesByUserId(Long userId);

    void syncArticlesByCategoryId(Long categoryId);

    void syncArticlesByTagId(Long tagId);

    void syncStudyQuestion(Long questionId);

    void deleteStudyQuestion(Long questionId);

    void syncStudyQuestionsByCategoryId(Long categoryId);

    Map<String, Object> rebuildIndex(String scope);

    Map<String, Object> getSearchStatus();
}
