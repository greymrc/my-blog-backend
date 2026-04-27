package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grey.myblog.dao.ArticleDAO;
import com.grey.myblog.dao.CategoryDAO;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.model.dataobject.ArticleDO;
import com.grey.myblog.model.dataobject.CategoryDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.CategoryAddRequest;
import com.grey.myblog.model.request.CategoryPageListRequest;
import com.grey.myblog.model.request.CategoryUpdateRequest;
import com.grey.myblog.model.response.CategoryResponse;
import com.grey.myblog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author grey
* @description 针对表【category(文章分类表)】的数据库操作Service实现
* @createDate 2026-01-15 11:49:53
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDAO, CategoryDO>
    implements CategoryService {

    private static final int MAX_CATEGORY_NAME_LENGTH = 50;
    private static final int DEFAULT_SORT_ORDER = 0;

    @Resource
    private ArticleDAO articleDAO;

    @Override
    public Page<CategoryResponse> listCategoryPage(CategoryPageListRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }

        QueryWrapper<CategoryDO> queryWrapper = buildQueryWrapper(request);
        Page<CategoryDO> categoryPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<CategoryResponse> categoryResponseList = categoryPage.getRecords().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());

        Page<CategoryResponse> responsePage = new Page<>(pageNum, pageSize, categoryPage.getTotal());
        responsePage.setRecords(categoryResponseList);
        return responsePage;
    }

    @Override
    public List<CategoryResponse> listAllCategories() {
        return this.list(new LambdaQueryWrapper<CategoryDO>()
                        .orderByDesc(CategoryDO::getSortOrder)
                        .orderByDesc(CategoryDO::getUpdateTime))
                .stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        CategoryDO category = getExistingCategory(id);
        return convertToCategoryResponse(category);
    }

    @Override
    public Long addCategory(CategoryAddRequest request) {
        validateCategoryAddRequest(request);
        String categoryName = normalizeCategoryName(request.getName());
        checkCategoryNameUnique(categoryName, null);

        CategoryDO category = new CategoryDO();
        category.setName(categoryName);
        category.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());

        boolean result = this.save(category);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增分类失败");
        }
        return category.getId();
    }

    @Override
    public Boolean updateCategory(CategoryUpdateRequest request) {
        validateCategoryUpdateRequest(request);
        CategoryDO existingCategory = getExistingCategory(request.getId());
        String categoryName = normalizeCategoryName(request.getName());
        checkCategoryNameUnique(categoryName, existingCategory.getId());

        CategoryDO category = new CategoryDO();
        category.setId(existingCategory.getId());
        category.setName(categoryName);
        category.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        category.setUpdateTime(new Date());

        boolean result = this.updateById(category);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新分类失败");
        }
        return true;
    }

    @Override
    public Boolean deleteCategory(Long id) {
        CategoryDO existingCategory = getExistingCategory(id);
        long articleCount = articleDAO.selectCount(
                new LambdaQueryWrapper<ArticleDO>()
                        .eq(ArticleDO::getCategoryId, existingCategory.getId())
        );
        if (articleCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "分类已被文章使用，暂不允许删除");
        }

        boolean result = this.removeById(existingCategory.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除分类失败");
        }
        return true;
    }

    /**
     * 构建分页查询条件
     */
    private QueryWrapper<CategoryDO> buildQueryWrapper(CategoryPageListRequest request) {
        QueryWrapper<CategoryDO> queryWrapper = new QueryWrapper<>();
        String categoryName = StrUtil.trim(request.getName());
        queryWrapper.lambda().like(StrUtil.isNotBlank(categoryName), CategoryDO::getName, categoryName);

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder) || "asc".equalsIgnoreCase(sortOrder);

        if (StrUtil.isBlank(sortField)) {
            queryWrapper.orderByDesc("sort_order").orderByDesc("update_time");
            return queryWrapper;
        }

        switch (sortField) {
            case "name" -> queryWrapper.orderBy(true, isAsc, "name");
            case "sortOrder", "sort_order" -> queryWrapper.orderBy(true, isAsc, "sort_order");
            case "createTime", "create_time" -> queryWrapper.orderBy(true, isAsc, "create_time");
            case "updateTime", "update_time" -> queryWrapper.orderBy(true, isAsc, "update_time");
            default -> queryWrapper.orderByDesc("sort_order").orderByDesc("update_time");
        }
        return queryWrapper;
    }

    /**
     * 校验分类新增请求
     */
    private void validateCategoryAddRequest(CategoryAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        validateCategoryName(request.getName());
    }

    /**
     * 校验分类更新请求
     */
    private void validateCategoryUpdateRequest(CategoryUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        if (request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类ID非法");
        }
        validateCategoryName(request.getName());
    }

    /**
     * 校验分类名称
     */
    private void validateCategoryName(String categoryName) {
        String normalizedCategoryName = normalizeCategoryName(categoryName);
        if (StrUtil.isBlank(normalizedCategoryName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        }
        if (normalizedCategoryName.length() > MAX_CATEGORY_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称长度不能超过50");
        }
    }

    /**
     * 校验分类名称唯一
     */
    private void checkCategoryNameUnique(String categoryName, Long excludeId) {
        LambdaQueryWrapper<CategoryDO> queryWrapper = new LambdaQueryWrapper<CategoryDO>()
                .eq(CategoryDO::getName, categoryName);
        queryWrapper.ne(excludeId != null, CategoryDO::getId, excludeId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称已存在");
        }
    }

    /**
     * 查询存在的分类
     */
    private CategoryDO getExistingCategory(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类ID非法");
        }
        CategoryDO category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        }
        return category;
    }

    /**
     * 转换为分类响应对象
     */
    private CategoryResponse convertToCategoryResponse(CategoryDO category) {
        CategoryResponse categoryResponse = new CategoryResponse();
        BeanUtils.copyProperties(category, categoryResponse);
        return categoryResponse;
    }

    /**
     * 标准化分类名称
     */
    private String normalizeCategoryName(String categoryName) {
        return StrUtil.trim(categoryName);
    }

    /**
     * 标准化排序权重
     */
    private Integer normalizeSortOrder(Integer sortOrder) {
        return sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
    }

}
