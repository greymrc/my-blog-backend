package com.grey.myblog.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.model.dataobject.ArticleDO;
import com.grey.myblog.model.request.ArticlePageListRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author grey
* @description 针对表【article(文章表)】的数据库操作Mapper
* @createDate 2026-01-15 11:36:14
* @Entity com.grey.myblog.model.dataobject.ArticleDO
*/
public interface ArticleMapper extends BaseMapper<ArticleDO> {

    /**
     * 自定义分页查询文章列表
     *
     * @param page 分页对象
     * @param request 查询请求参数
     * @return 分页结果
     */
    Page<ArticleDO> selectArticlePage(Page<ArticleDO> page, @Param("request") ArticlePageListRequest request);
}




