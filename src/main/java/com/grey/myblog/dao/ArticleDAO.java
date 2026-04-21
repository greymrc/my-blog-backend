package com.grey.myblog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.model.dataobject.ArticleDO;
import com.grey.myblog.model.request.ArticlePageListRequest;
import org.apache.ibatis.annotations.Param;

/**
* @author grey
* @description 针对表【article(文章表)】的数据库操作DAO
* @createDate 2026-01-15 11:36:14
* @Entity com.grey.myblog.model.dataobject.ArticleDO
*/
public interface ArticleDAO extends BaseMapper<ArticleDO> {

    /**
     * 自定义分页查询文章列表
     *
     * @param page 分页对象
     * @param request 查询请求参数
     * @return 分页结果
     */
    Page<ArticleDO> selectArticlePage(Page<ArticleDO> page, @Param("request") ArticlePageListRequest request);
}
