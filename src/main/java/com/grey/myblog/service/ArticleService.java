package com.grey.myblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.model.dataobject.ArticleDO;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.request.ArticleAddRequest;
import com.grey.myblog.model.request.ArticlePageListRequest;
import com.grey.myblog.model.request.ArticleUpdateRequest;
import com.grey.myblog.model.response.ArticleResponse;
import com.grey.myblog.model.response.ArticleArchiveResponse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 文章服务接口
 *
 * @author grey
 */
public interface ArticleService extends IService<ArticleDO> {

    /**
     * 分页查询文章列表 (文章只返回摘要，不返回完整内容)
     *
     * @param request 分页查询请求
     * @return 分页文章列表
     */
    Page<ArticleResponse> listArticles(ArticlePageListRequest request);

    /**
     * 获取文章详情
     * 查询文章完整信息（包含正文），自动增加阅读量，并填充关联数据（分类、作者、标签）
     *
     * @param id 文章ID
     * @return 文章详情VO，包含完整内容和关联信息
     */
    ArticleResponse getArticleById(Long id);

    /**
     * 获取文章归档列表（轻量级）
     * 只查询必要字段（id、articleTitle、createTime），填充分类和标签信息，按年月分组返回
     *
     * @param year  年份（可选），筛选指定年份的文章
     * @param month 月份（可选），筛选指定月份的文章
     * @return 按年月分组的文章归档列表，格式：Map<年份, Map<月份, List<文章归档VO>>>
     */
    Map<String, Map<String, List<ArticleArchiveResponse>>> getArticleArchive(Integer year, Integer month);

    /**
     * 创建文章
     * 校验请求参数，创建文章实体并保存，如果指定了标签则保存标签关联关系
     *
     * @param request   创建请求，包含标题、内容、分类、标签等信息
     * @param loginUser 当前登录用户，作为文章作者
     * @return 创建成功的文章ID
     */
    Long addArticle(ArticleAddRequest request, UserDO loginUser);

    /**
     * 更新文章
     * 校验参数和权限，更新文章内容，删除旧标签关联并保存新标签关联关系
     *
     * @param request   更新请求，包含文章ID和要更新的内容
     * @param loginUser 当前登录用户，用于权限校验
     * @return 是否更新成功
     */
    Boolean updateArticle(ArticleUpdateRequest request, UserDO loginUser);

    /**
     * 删除文章
     * 校验参数和权限，执行逻辑删除（设置isDelete标记）
     *
     * @param id        文章ID
     * @param loginUser 当前登录用户，用于权限校验
     * @return 是否删除成功
     */
    Boolean deleteArticle(Long id, UserDO loginUser);

    /**
     * 增加阅读量
     *
     * @param id 文章ID
     * @return 是否成功
     */
    Boolean incrementViewCount(Long id);
}
