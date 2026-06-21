package com.grey.myblog.service;

import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.dto.ArticleArchiveDTO;
import com.grey.myblog.model.dto.ArticleDTO;
import com.grey.myblog.model.request.ArticleAddRequest;
import com.grey.myblog.model.request.ArticlePageListRequest;
import com.grey.myblog.model.request.ArticleUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * 文章服务接口
 *
 * @author grey
 */
public interface ArticleService {

    /**
     * 分页查询文章列表
     */
    PageResult<ArticleDTO> listArticles(ArticlePageListRequest request);

    /**
     * 获取文章详情
     */
    ArticleDTO getArticleById(Long id);

    /**
     * 获取文章归档列表
     */
    Map<String, Map<String, List<ArticleArchiveDTO>>> getArticleArchive(Integer year, Integer month);

    /**
     * 创建文章
     */
    Long addArticle(ArticleAddRequest request, UserDO loginUser);

    /**
     * 更新文章
     */
    Boolean updateArticle(ArticleUpdateRequest request, UserDO loginUser);

    /**
     * 删除文章
     */
    Boolean deleteArticle(Long id, UserDO loginUser);

    /**
     * 增加阅读量
     */
    Boolean incrementViewCount(Long id);
}