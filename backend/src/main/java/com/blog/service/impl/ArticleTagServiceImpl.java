package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.ArticleTag;
import com.blog.mapper.ArticleTagMapper;
import com.blog.service.ArticleTagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {

    @Override
    public List<Long> getArticleIdsByTagId(Long tagId) {
        return list(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId))
                .stream()
                .map(ArticleTag::getArticleId)
                .collect(Collectors.toList());
    }
}
