package com.grey.myblog.service;

import com.grey.myblog.model.dataobject.ArticleTagDO;

import java.util.List;

/**
 * 文章-标签关联服务接口
 *
 * @author grey
 */
public interface ArticleTagService {

    /**
     * 保存关联
     */
    int save(ArticleTagDO articleTag);

    /**
     * 批量保存关联
     */
    int saveBatch(List<ArticleTagDO> articleTags);

    /**
     * 根据文章ID删除所有关联
     */
    int removeByArticleId(Long articleId);

    /**
     * 根据标签ID删除所有关联
     */
    int removeByTagId(Long tagId);

    /**
     * 根据文章ID查询关联列表
     */
    List<ArticleTagDO> listByArticleId(Long articleId);

    /**
     * 根据标签ID查询关联列表
     */
    List<ArticleTagDO> listByTagId(Long tagId);

    /**
     * 根据文章ID批量查询关联
     */
    List<ArticleTagDO> listByArticleIds(List<Long> articleIds);

    /**
     * 根据标签ID统计关联数量
     */
    long countByTagId(Long tagId);
}