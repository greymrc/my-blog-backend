package com.grey.myblog.dao;

import com.grey.myblog.model.dataobject.ArticleTagDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章-标签关联表 DAO
 *
 * @author grey
 */
public interface ArticleTagDAO {

    /**
     * 插入关联
     */
    int insert(ArticleTagDO articleTag);

    /**
     * 批量插入关联
     */
    int insertBatch(@Param("list") List<ArticleTagDO> articleTags);

    /**
     * 根据文章ID删除所有关联
     */
    int deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据标签ID删除所有关联
     */
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 根据ID查询关联
     */
    ArticleTagDO selectById(@Param("id") Long id);

    /**
     * 根据文章ID查询关联列表
     */
    List<ArticleTagDO> selectByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据标签ID查询关联列表
     */
    List<ArticleTagDO> selectByTagId(@Param("tagId") Long tagId);

    /**
     * 根据文章ID批量查询关联
     */
    List<ArticleTagDO> selectByArticleIds(@Param("articleIds") List<Long> articleIds);

    /**
     * 查询关联列表
     */
    List<ArticleTagDO> selectList(@Param("articleTag") ArticleTagDO articleTag);

    /**
     * 根据标签ID统计关联数量
     */
    long countByTagId(@Param("tagId") Long tagId);
}