package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grey.myblog.dao.ArticleDAO;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.exception.ThrowUtil;
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
public class ArticleServiceImpl extends ServiceImpl<ArticleDAO, ArticleDO>
        implements ArticleService {

    @Resource
    private CategoryService categoryService;

    @Resource
    private TagService tagService;

    @Resource
    private ArticleTagService articleTagService;

    @Resource
    private UserService userService;

    @Override
    public Page<ArticleResponse> listArticles(ArticlePageListRequest request) {
        // 参数非空校验
        ThrowUtil.throwIf(request==null, ErrorCode.PARAMS_ERROR);

        // 参数校验：页码和每页数量不能小于1，设置默认值
        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }

        try {
            // 使用自定义 SQL 执行分页查询
            Page<ArticleDO> articlePage = new Page<>(pageNum, pageSize);
            Page<ArticleDO> resultPage = baseMapper.selectArticlePage(articlePage, request);
            
            // 转换为VO对象
            List<ArticleResponse> articleVOList = resultPage.getRecords().stream()
                    .map(this::convertToArticleResponse)
                    .collect(Collectors.toList());
            
            // 填充关联数据（分类、作者、标签）
            fillAssociatedData(articleVOList);
            
            // 构建分页结果
            Page<ArticleResponse> articleVOPage = new Page<>(pageNum, pageSize, resultPage.getTotal());
            articleVOPage.setRecords(articleVOList);
            return articleVOPage;
        } catch (Exception e) {
            log.error("分页查询文章列表异常：", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "分页查询失败");
        }
    }

    /**
     * 获取文章详情
     * 查询文章实体，自动增加阅读量，转换为VO并填充关联数据
     */
    @Override
    public ArticleResponse getArticleById(Long id) {
        ThrowUtil.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "文章ID无效");

        ArticleDO article = this.getById(id);
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

    /**
     * 获取文章归档列表（轻量级）
     * 只查询必要字段（id、articleTitle、createTime、categoryId），填充分类和标签信息，按年月分组返回
     */
    @Override
    public Map<String, Map<String, List<ArticleArchiveResponse>>> getArticleArchive(Integer year, Integer month) {
        // 构建查询条件：只查询公开文章，只查询必要字段
        QueryWrapper<ArticleDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .select(ArticleDO::getId, ArticleDO::getArticleTitle, ArticleDO::getCreateTime, ArticleDO::getCategoryId)
                .eq(ArticleDO::getStatus, 1);
        
        // 按年份筛选（如果指定）
        if (year != null) {
            queryWrapper.lambda().apply("YEAR(create_time) = {0}", year);
        }
        // 按年份、月份筛选（如果指定）
        if (year!= null && month != null) {
            queryWrapper.lambda().apply("MONTH(create_time) = {0}", month);
        }
        
        // 按创建时间倒序排序
        queryWrapper.lambda().orderByDesc(ArticleDO::getCreateTime);

        // 查询文章列表（只包含必要字段）
        List<ArticleDO> articles = this.list(queryWrapper);

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
        
        // 批量填充分类和标签信息（不填充作者信息）
        fillArchiveAssociatedData(archiveVOList, articleCategoryMap);
        
        // 按年月分组：Map<年份, Map<月份, List<文章归档VO>>>
        Map<String, Map<String, List<ArticleArchiveResponse>>> archiveMap = new LinkedHashMap<>();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        
        for (ArticleArchiveResponse archiveVO : archiveVOList) {
            // 跳过创建时间为空的文章
            if (archiveVO.getCreateTime() == null) {
                continue;
            }
            
            // 提取年份和月份字符串
            String yearStr = yearFormat.format(archiveVO.getCreateTime());
            String monthStr = monthFormat.format(archiveVO.getCreateTime());
            
            // 按年月分组，使用 computeIfAbsent 自动创建嵌套 Map 和 List
            archiveMap.computeIfAbsent(yearStr, k -> new LinkedHashMap<>())
                    .computeIfAbsent(monthStr, k -> new ArrayList<>())
                    .add(archiveVO);
        }
        
        return archiveMap;
    }

    /**
     * 创建文章
     * 校验参数，创建文章实体并保存，如果指定了标签则批量保存标签关联关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addArticle(ArticleAddRequest request, UserDO loginUser) {
        // 校验请求参数（标题、内容等）
        validateArticleRequest(request);
        
        // 创建文章实体，复制请求参数并设置默认值
        ArticleDO article = new ArticleDO();
        BeanUtils.copyProperties(request, article);
        article.setAuthorId(loginUser.getId());
        article.setViewCount(0);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());
        
        // 保存文章
        boolean saved = this.save(article);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建文章失败");
        }
        
        // 如果指定了标签，批量保存文章-标签关联关系
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), request.getTagIds());
        }
        
        return article.getId();
    }

    /**
     * 更新文章
     * 校验参数和权限，更新文章内容，删除旧标签关联并保存新标签关联关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateArticle(ArticleUpdateRequest request, UserDO loginUser) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID不能为空");
        }
        
        // 校验请求参数（标题、内容等）
        validateArticleRequest(request);
        
        // 查询原文章，检查是否存在
        ArticleDO existingArticle = this.getById(request.getId());
        if (existingArticle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }
        
        // 权限校验：管理员或文章作者才能修改
        if (!userService.isAdmin(loginUser) && !existingArticle.getAuthorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改此文章");
        }
        
        // 更新文章内容
        ArticleDO article = new ArticleDO();
        BeanUtils.copyProperties(request, article);
        article.setUpdateTime(new Date());
        
        boolean updated = this.updateById(article);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新文章失败");
        }
        
        // 删除旧标签关联，重新保存新标签关联关系
        deleteArticleTags(request.getId());
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(request.getId(), request.getTagIds());
        }
        
        return true;
    }

    /**
     * 删除文章
     * 校验参数和权限，执行逻辑删除（MyBatis-Plus会自动处理 isDeleted 标记）
     */
    @Override
    public Boolean deleteArticle(Long id, UserDO loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        
        // 查询文章，检查是否存在
        ArticleDO article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }
        
        // 权限校验：管理员或文章作者才能删除
        if (!userService.isAdmin(loginUser) && !article.getAuthorId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除此文章");
        }
        
        // 执行逻辑删除（MyBatis-Plus 会自动设置 isDeleted=1）
        return this.removeById(id);
    }

    /**
     * 增加文章阅读量
     * 每次访问文章详情时调用，阅读量+1
     */
    @Override
    public Boolean incrementViewCount(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        ArticleDO article = this.getById(id);
        if (article == null) {
            return false;
        }
        
        article.setViewCount(article.getViewCount() + 1);
        return this.updateById(article);
    }


    /**
     * 将文章实体转换为VO对象
     * 复制基本属性，并计算文章字数统计
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
     * 采用批量查询策略，一次性查询所有分类、作者、标签，避免N+1查询问题
     * 填充分类信息、作者信息、标签列表到ArticleResponse中
     * TODO 可优化，标签、分类、作者均可实现缓存。
     */
    private void fillAssociatedData(List<ArticleResponse> articleVOList) {
        if (articleVOList == null || articleVOList.isEmpty()) {
            return;
        }
        
        // 收集所有需要查询的ID（分类ID、作者ID、文章ID）
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
        
        // 批量查询分类信息，构建ID到VO的映射
        Map<Long, CategoryResponse> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<CategoryDO> categories = categoryService.listByIds(categoryIds);
            categoryMap = categories.stream()
                    .map(this::convertToCategoryResponse)
                    .collect(Collectors.toMap(CategoryResponse::getId, vo -> vo));
        }
        
        // 批量查询作者信息，构建ID到VO的映射
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
            // 查询文章-标签关联表
            List<ArticleTagDO> articleTags = articleTagService.list(
                    new LambdaQueryWrapper<ArticleTagDO>()
                            .in(ArticleTagDO::getArticleId, articleIds)
            );
            
            // 提取标签ID集合
            Set<Long> tagIds = articleTags.stream()
                    .map(ArticleTagDO::getTagId)
                    .collect(Collectors.toSet());
            
            // 批量查询标签信息
            if (!tagIds.isEmpty()) {
                List<TagDO> tags = tagService.listByIds(tagIds);
                Map<Long, TagResponse> tagMap = tags.stream()
                        .map(this::convertToTagResponse)
                        .collect(Collectors.toMap(TagResponse::getId, vo -> vo));
                
                // 构建文章ID到标签列表的映射
                for (ArticleTagDO articleTag : articleTags) {
                    TagResponse tagVO = tagMap.get(articleTag.getTagId());
                    if (tagVO != null) {
                        articleTagMap.computeIfAbsent(articleTag.getArticleId(), k -> new ArrayList<>())
                                .add(tagVO);
                    }
                }
            }
        }
        
        // 将查询到的关联数据填充到ArticleResponse中
        for (ArticleResponse articleVO : articleVOList) {
            if (articleVO.getCategoryId() != null) {
                CategoryResponse categoryVO = categoryMap.get(articleVO.getCategoryId());
                articleVO.setCategory(categoryVO);
            }
            
            if (articleVO.getAuthorId() != null) {
                ArticleResponse.AuthorResponse authorVO = authorMap.get(articleVO.getAuthorId());
                articleVO.setAuthor(authorVO);
            }
            
            // 设置标签列表，如果为空则设置为空列表
            List<TagResponse> tags = articleTagMap.get(articleVO.getId());
            articleVO.setTags(tags != null ? tags : new ArrayList<>());
        }
    }

    /**
     * 批量保存文章标签关联关系
     * 为文章创建与多个标签的关联记录
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
     * 删除文章的所有标签关联关系
     * 用于更新文章时清除旧标签
     */
    private void deleteArticleTags(Long articleId) {
        articleTagService.remove(
                new LambdaQueryWrapper<ArticleTagDO>()
                        .eq(ArticleTagDO::getArticleId, articleId)
        );
    }

    /**
     * 校验文章创建请求参数
     * 检查请求对象和公共字段（标题、内容）
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
     * 检查标题和内容不能为空
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
     * 只复制必要字段：id、articleTitle、createTime
     *
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
     * 批量填充归档文章的关联数据（只填充分类和标签，不填充作者）
     * 采用批量查询策略，避免N+1查询问题
     * TODO 可优化，标签、分类、作者均可实现缓存。
     * @param archiveVOList 归档VO列表
     * @param articleCategoryMap 文章ID到分类ID的映射
     */
    private void fillArchiveAssociatedData(List<ArticleArchiveResponse> archiveVOList, Map<Long, Long> articleCategoryMap) {
        if (archiveVOList == null || archiveVOList.isEmpty()) {
            return;
        }
        
        // 收集所有需要查询的分类ID
        Set<Long> categoryIds = new HashSet<>(articleCategoryMap.values());
        
        // 批量查询分类信息
        Map<Long, CategoryResponse> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<CategoryDO> categories = categoryService.listByIds(categoryIds);
            categoryMap = categories.stream()
                    .map(this::convertToCategoryResponse)
                    .collect(Collectors.toMap(CategoryResponse::getId, vo -> vo));
        }
        
        // 收集所有文章ID
        List<Long> articleIds = archiveVOList.stream()
                .map(ArticleArchiveResponse::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 批量查询文章标签关联关系
        Map<Long, List<TagResponse>> articleTagMap = new HashMap<>();
        if (!articleIds.isEmpty()) {
            // 查询文章-标签关联表
            List<ArticleTagDO> articleTags = articleTagService.list(
                    new LambdaQueryWrapper<ArticleTagDO>()
                            .in(ArticleTagDO::getArticleId, articleIds)
            );
            
            // 提取标签ID集合
            Set<Long> tagIds = articleTags.stream()
                    .map(ArticleTagDO::getTagId)
                    .collect(Collectors.toSet());
            
            // 批量查询标签信息
            if (!tagIds.isEmpty()) {
                List<TagDO> tags = tagService.listByIds(tagIds);
                Map<Long, TagResponse> tagMap = tags.stream()
                        .map(this::convertToTagResponse)
                        .collect(Collectors.toMap(TagResponse::getId, vo -> vo));
                
                // 构建文章ID到标签列表的映射
                for (ArticleTagDO articleTag : articleTags) {
                    TagResponse tagVO = tagMap.get(articleTag.getTagId());
                    if (tagVO != null) {
                        articleTagMap.computeIfAbsent(articleTag.getArticleId(), k -> new ArrayList<>())
                                .add(tagVO);
                    }
                }
            }
        }
        
        // 将查询到的关联数据填充到ArticleArchiveResponse中
        for (ArticleArchiveResponse archiveVO : archiveVOList) {
            // 填充分类信息
            Long categoryId = articleCategoryMap.get(archiveVO.getId());
            if (categoryId != null) {
                CategoryResponse categoryVO = categoryMap.get(categoryId);
                archiveVO.setCategory(categoryVO);
            }
            
            // 填充标签列表，如果为空则设置为空列表
            List<TagResponse> tags = articleTagMap.get(archiveVO.getId());
            archiveVO.setTags(tags != null ? tags : new ArrayList<>());
        }
    }
}
