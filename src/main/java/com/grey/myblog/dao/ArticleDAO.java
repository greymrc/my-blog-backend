package com.grey.myblog.dao;

import com.grey.myblog.model.dataobject.ArticleDO;
import com.grey.myblog.model.request.ArticlePageListRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章表 DAO
 *
 * @author grey
 */
public interface ArticleDAO {

    /**
     * 插入文章
     */
    int insert(ArticleDO article);

    /**
     * 批量插入文章
     */
    int insertBatch(@Param("list") List<ArticleDO> articles);

    /**
     * 根据ID更新文章
     */
    int updateById(ArticleDO article);

    /**
     * 根据ID逻辑删除文章
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询文章
     */
    ArticleDO selectById(@Param("id") Long id);

    /**
     * 根据ID批量查询文章
     */
    List<ArticleDO> selectBatchIds(@Param("ids") List<Long> ids);

    /**
     * 查询文章列表
     */
    List<ArticleDO> selectList(@Param("article") ArticleDO article);

    /**
     * 查询文章总数
     */
    long selectCount(@Param("article") ArticleDO article);

    /**
     * 分页查询文章列表（带条件）
     */
    List<ArticleDO> selectArticlePage(@Param("request") ArticlePageListRequest request);

    /**
     * 增加阅读量
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 根据状态查询文章列表（用于归档）
     */
    List<ArticleDO> selectByStatus(@Param("status") Integer status, @Param("year") Integer year, @Param("month") Integer month);
}