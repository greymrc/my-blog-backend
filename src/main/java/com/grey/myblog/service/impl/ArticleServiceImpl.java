package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.grey.myblog.dao.ArticleDAO;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.exception.AssertUtil;
import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.ArticleDO;
import com.grey.myblog.model.dataobject.ArticleTagDO;
import com.grey.myblog.model.dataobject.CategoryDO;
import com.grey.myblog.model.dataobject.TagDO;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.ArticleAddRequest;
import com.grey.myblog.model.request.ArticlePageListRequest;
import com.grey.myblog.model.request.ArticleUpdateRequest;
import com.grey.myblog.model.response.ArticleResponse;
import com.grey.myblog.model.response.ArticleArchiveResponse;
import com.grey.myblog.model.response.CategoryResponse;
import com.grey.myblog.model.response.TagResponse;
import com.grey.myblog.service.ArticleService;
import com.grey.myblog.service.ArticleTagService;
import com.grey.myblog.service.CategoryService;
import com.grey.myblog.service.TagService;
import com.grey.myblog.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 *
 * @author grey
 */
@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleDAO articleDAO;

    @Resource
    private CategoryService categoryService;

    @Resource
    private TagService tagService;

    @Resource
    private ArticleTagService articleTagService;

    @Resource
    private UserService userService;

    @Override
    public PageResult<ArticleResponse> listArticles(ArticlePageListRequest request) {
        // 参数非空校验
        AssertUtil.isFalse(request == null, ErrorCode.PARAMS_ERROR);

        // 参数校验：页码和每页数量不能小于1，设置默认值
        int pageNum = request.getPageNum() < 1 ? 1 : (int) request.getPageNum();
        int pageSize = request.getPageSize() < 1 ? 10 : (int) request.getPageSize();

        try {
            PageHelper.startPage(pageNum, pageSize);
            List<ArticleDO> articleList = articleDAO.selectArticlePage(request);
            PageInfo<ArticleDO> pageInfo = new PageInfo<>(articleList);

            // 转换为VO对象
            List<ArticleResponse> articleVOList = articleList.stream()
                    .map(this::convertToArticleResponse)
                    .collect(Collectors.toList());

            // 填充关联数据（分类、作者、标签）
            fillAssociatedData(articleVOList);

            return new PageResult<>(pageNum, pageSize, pageInfo.getTotal(), articleVOList);
        } catch (Exception e) {
            log.error("分页查询文章列表异常：", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "分页查询失败");
        }
    }

    @Override
    public ArticleResponse getArticleById(Long id) {
        AssertUtil.isFalse(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "文章ID无效");

        ArticleDO article = articleDAO.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        // 增加阅读量
        incrementViewCount(id);

        // 转换为VO并填充关联数据
        ArticleResponse articleVO = convertToArticleResponse(article);
        fillAssociatedData(Collections.singletonList(articleVO));
        return articleVO;
    }

    @Override
    public Map<String, Map<String, List<ArticleArchiveResponse>>> getArticleArchive(Integer year, Integer month) {
        // 查询公开文章，只查询必要字段
        List<ArticleDO> articles = articleDAO.selectByStatus(1, year, month);

        // 转换为轻量级归档VO，并保存articleId到categoryId的映射
        Map<Long, Long> articleCategoryMap = new HashMap<>();
        List<ArticleArchiveResponse> archiveVOList = articles.stream()
                .map(article -> {
                    ArticleArchiveResponse archiveVO = convertToArticleArchiveResponse(article);
                    if (article.getCategoryId() != null) {
                        articleCategoryMap.put(archiveVO.getId(), article.getCategoryId());
                    }
                    return archiveVO;
                })
                .collect(Collectors.toList());

        // 批量填充分类和标签信息
        fillArchiveAssociatedData(archiveVOList, articleCategoryMap);

        // 按年月分组
        Map<String, Map<String, List<ArticleArchiveResponse>>> archiveMap = new LinkedHashMap<>();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");

        for (ArticleArchiveResponse archiveVO : archiveVOList) {
            if (archiveVO.getCreateTime() == null) {
                continue;
            }

            String yearStr = yearFormat.format(archiveVO.getCreateTime());
            String monthStr = monthFormat.format(archiveVO.getCreateTime());

            archiveMap.computeIfAbsent(yearStr, k -> new LinkedHashMap<>())
                    .computeIfAbsent(monthStr, k -> new ArrayList<>())
                    .add(archiveVO);
        }

        return archiveMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addArticle(ArticleAddRequest request, UserDO loginUser) {
        validateArticleRequest(request);

        ArticleDO article = new ArticleDO();
        BeanUtils.copyProperties(request, article);
        article.setAuthorId(loginUser.getId());
        article.setViewCount(0);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());

        int result = articleDAO.insert(article);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建文章失败");
        }

        // 如果指定了标签，批量保存文章-标签关联关系
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), request.getTagIds());
        }

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateArticle(ArticleUpdateRequest request, UserDO loginUser) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID不能为空");
        }

        validateArticleRequest(request);

        ArticleDO existingArticle = articleDAO.selectById(request.getId());
        if (existingArticle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        // 权限校验
        if (!userService.isAdmin(loginUser) && !existingArticle.getAuthorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改此文章");
        }

        ArticleDO article = new ArticleDO();
        BeanUtils.copyProperties(request, article);
        article.setUpdateTime(new Date());

        int result = articleDAO.updateById(article);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新文章失败");
        }

        // 删除旧标签关联，重新保存新标签关联关系
        articleTagService.removeByArticleId(request.getId());
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(request.getId(), request.getTagIds());
        }

        return true;
    }

    @Override
    public Boolean deleteArticle(Long id, UserDO loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }

        ArticleDO article = articleDAO.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        // 权限校验
        if (!userService.isAdmin(loginUser) && !article.getAuthorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除此文章");
        }

        int result = articleDAO.deleteById(id);
        return result > 0;
    }

    @Override
    public Boolean incrementViewCount(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return articleDAO.incrementViewCount(id) > 0;
    }

    /**
     * 将文章实体转换为VO对象
     */
    private ArticleResponse convertToArticleResponse(ArticleDO article) {
        if (article == null) {
            return null;
        }

        ArticleResponse articleVO = new ArticleResponse();
        BeanUtils.copyProperties(article, articleVO);

        if (article.getArticleContent() != null) {
            articleVO.setWordCount(calculateWordCount(article.getArticleContent()));
        }

        return articleVO;
    }

    /**
     * 批量填充文章关联数据
     */
    private void fillAssociatedData(List<ArticleResponse> articleVOList) {
        if (articleVOList == null || articleVOList.isEmpty()) {
            return;
        }

        Set<Long> categoryIds = articleVOList.stream()
                .map(ArticleResponse::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> authorIds = articleVOList.stream()
                .map(ArticleResponse::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> articleIds = articleVOList.stream()
                .map(ArticleResponse::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 批量查询分类信息
        Map<Long, CategoryResponse> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<CategoryDO> categories = categoryService.listByIds(categoryIds);
            categoryMap = categories.stream()
                    .map(this::convertToCategoryResponse)
                    .collect(Collectors.toMap(CategoryResponse::getId, vo -> vo));
        }

        // 批量查询作者信息
        Map<Long, ArticleResponse.AuthorResponse> authorMap = new HashMap<>();
        if (!authorIds.isEmpty()) {
            List<UserDO> users = userService.listByIds(authorIds);
            authorMap = users.stream()
                    .map(this::convertToAuthorVO)
                    .collect(Collectors.toMap(ArticleResponse.AuthorResponse::getId, vo -> vo));
        }

        // 批量查询文章标签关联关系
        Map<Long, List<TagResponse>> articleTagMap = new HashMap<>();
        if (!articleIds.isEmpty()) {
            List<ArticleTagDO> articleTags = articleTagService.listByArticleIds(articleIds);

            Set<Long> tagIds = articleTags.stream()
                    .map(ArticleTagDO::getTagId)
                    .collect(Collectors.toSet());

            if (!tagIds.isEmpty()) {
                List<TagDO> tags = tagService.listByIds(tagIds);
                Map<Long, TagResponse> tagMap = tags.stream()
                        .map(this::convertToTagResponse)
                        .collect(Collectors.toMap(TagResponse::getId, vo -> vo));

                for (ArticleTagDO articleTag : articleTags) {
                    TagResponse tagVO = tagMap.get(articleTag.getTagId());
                    if (tagVO != null) {
                        articleTagMap.computeIfAbsent(articleTag.getArticleId(), k -> new ArrayList<>())
                                .add(tagVO);
                    }
                }
            }
        }

        // 填充关联数据
        for (ArticleResponse articleVO : articleVOList) {
            if (articleVO.getCategoryId() != null) {
                articleVO.setCategory(categoryMap.get(articleVO.getCategoryId()));
            }

            if (articleVO.getAuthorId() != null) {
                articleVO.setAuthor(authorMap.get(articleVO.getAuthorId()));
            }

            List<TagResponse> tags = articleTagMap.get(articleVO.getId());
            articleVO.setTags(tags != null ? tags : new ArrayList<>());
        }
    }

    /**
     * 批量保存文章标签关联关系
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        List<ArticleTagDO> articleTags = tagIds.stream()
                .map(tagId -> {
                    ArticleTagDO articleTag = new ArticleTagDO();
                    articleTag.setArticleId(articleId);
                    articleTag.setTagId(tagId);
                    articleTag.setCreateTime(new Date());
                    articleTag.setUpdateTime(new Date());
                    return articleTag;
                })
                .collect(Collectors.toList());

        articleTagService.saveBatch(articleTags);
    }

    /**
     * 校验文章创建请求参数
     */
    private void validateArticleRequest(ArticleAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        validateArticleCommonFields(request.getArticleTitle(), request.getArticleContent());
    }

    /**
     * 校验文章请求参数（更新）
     */
    private void validateArticleRequest(ArticleUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        validateArticleCommonFields(request.getArticleTitle(), request.getArticleContent());
    }

    /**
     * 校验文章公共字段
     */
    private void validateArticleCommonFields(String articleTitle, String articleContent) {
        if (StrUtil.isBlank(articleTitle)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章标题不能为空");
        }
        if (StrUtil.isBlank(articleContent)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章内容不能为空");
        }
    }

    /**
     * 计算字数
     */
    private Integer calculateWordCount(String articleContent) {
        if (articleContent == null) {
            return 0;
        }
        return articleContent.length();
    }

    /**
     * 转换为CategoryResponse
     */
    private CategoryResponse convertToCategoryResponse(CategoryDO category) {
        if (category == null) {
            return null;
        }
        CategoryResponse categoryVO = new CategoryResponse();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    /**
     * 转换为TagResponse
     */
    private TagResponse convertToTagResponse(TagDO tag) {
        if (tag == null) {
            return null;
        }
        TagResponse tagVO = new TagResponse();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }

    /**
     * 转换为AuthorVO
     */
    private ArticleResponse.AuthorResponse convertToAuthorVO(UserDO user) {
        if (user == null) {
            return null;
        }
        ArticleResponse.AuthorResponse authorVO = new ArticleResponse.AuthorResponse();
        authorVO.setId(user.getId());
        authorVO.setNickname(user.getNickname());
        authorVO.setAvatar(user.getAvatar());
        return authorVO;
    }

    /**
     * 将文章实体转换为归档VO对象
     */
    private ArticleArchiveResponse convertToArticleArchiveResponse(ArticleDO article) {
        if (article == null) {
            return null;
        }
        ArticleArchiveResponse archiveVO = new ArticleArchiveResponse();
        archiveVO.setId(article.getId());
        archiveVO.setArticleTitle(article.getArticleTitle());
        archiveVO.setCreateTime(article.getCreateTime());
        return archiveVO;
    }

    /**
     * 批量填充归档文章的关联数据
     */
    private void fillArchiveAssociatedData(List<ArticleArchiveResponse> archiveVOList, Map<Long, Long> articleCategoryMap) {
        if (archiveVOList == null || archiveVOList.isEmpty()) {
            return;
        }

        Set<Long> categoryIds = new HashSet<>(articleCategoryMap.values());

        Map<Long, CategoryResponse> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<CategoryDO> categories = categoryService.listByIds(categoryIds);
            categoryMap = categories.stream()
                    .map(this::convertToCategoryResponse)
                    .collect(Collectors.toMap(CategoryResponse::getId, vo -> vo));
        }

        List<Long> articleIds = archiveVOList.stream()
                .map(ArticleArchiveResponse::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, List<TagResponse>> articleTagMap = new HashMap<>();
        if (!articleIds.isEmpty()) {
            List<ArticleTagDO> articleTags = articleTagService.listByArticleIds(articleIds);

            Set<Long> tagIds = articleTags.stream()
                    .map(ArticleTagDO::getTagId)
                    .collect(Collectors.toSet());

            if (!tagIds.isEmpty()) {
                List<TagDO> tags = tagService.listByIds(tagIds);
                Map<Long, TagResponse> tagMap = tags.stream()
                        .map(this::convertToTagResponse)
                        .collect(Collectors.toMap(TagResponse::getId, vo -> vo));

                for (ArticleTagDO articleTag : articleTags) {
                    TagResponse tagVO = tagMap.get(articleTag.getTagId());
                    if (tagVO != null) {
                        articleTagMap.computeIfAbsent(articleTag.getArticleId(), k -> new ArrayList<>())
                                .add(tagVO);
                    }
                }
            }
        }

        for (ArticleArchiveResponse archiveVO : archiveVOList) {
            Long categoryId = articleCategoryMap.get(archiveVO.getId());
            if (categoryId != null) {
                archiveVO.setCategory(categoryMap.get(categoryId));
            }

            List<TagResponse> tags = articleTagMap.get(archiveVO.getId());
            archiveVO.setTags(tags != null ? tags : new ArrayList<>());
        }
    }
}