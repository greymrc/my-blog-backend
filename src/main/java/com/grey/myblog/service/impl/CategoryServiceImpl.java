package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.grey.myblog.dao.CategoryDAO;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.CategoryDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.CategoryAddRequest;
import com.grey.myblog.model.request.CategoryPageListRequest;
import com.grey.myblog.model.request.CategoryUpdateRequest;
import com.grey.myblog.model.dto.CategoryDTO;
import com.grey.myblog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 *
 * @author grey
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int MAX_CATEGORY_NAME_LENGTH = 50;
    private static final int DEFAULT_SORT_ORDER = 0;

    @Resource
    private CategoryDAO categoryDAO;

    @Override
    public PageResult<CategoryDTO> listCategoryPage(CategoryPageListRequest request) {
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

        PageHelper.startPage((int) pageNum, (int) pageSize);
        List<CategoryDO> categoryList = categoryDAO.selectCategoryPage(request);
        PageInfo<CategoryDO> pageInfo = new PageInfo<>(categoryList);

        List<CategoryDTO> categoryResponseList = categoryList.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());

        return new PageResult<>(pageNum, pageSize, pageInfo.getTotal(), categoryResponseList);
    }

    @Override
    public List<CategoryDTO> listAllCategories() {
        List<CategoryDO> categoryList = categoryDAO.selectList(new CategoryDO());
        return categoryList.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        CategoryDO category = getExistingCategory(id);
        return convertToCategoryDTO(category);
    }

    @Override
    public Long addCategory(CategoryAddRequest request) {
        validateCategoryAddRequest(request);
        String categoryName = normalizeCategoryName(request.getName());
        checkCategoryNameUnique(categoryName, null);

        CategoryDO category = CategoryDO.builder()
                .name(categoryName)
                .sortOrder(normalizeSortOrder(request.getSortOrder()))
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        int result = categoryDAO.insert(category);
        if (result <= 0) {
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

        CategoryDO category = CategoryDO.builder()
                .id(existingCategory.getId())
                .name(categoryName)
                .sortOrder(normalizeSortOrder(request.getSortOrder()))
                .updateTime(new Date())
                .build();

        int result = categoryDAO.updateById(category);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新分类失败");
        }
        return true;
    }

    @Override
    public Boolean deleteCategory(Long id) {
        CategoryDO existingCategory = getExistingCategory(id);
        long articleCount = categoryDAO.countArticlesByCategoryId(existingCategory.getId());
        if (articleCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "分类已被文章使用，暂不允许删除");
        }

        int result = categoryDAO.deleteById(existingCategory.getId());
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除分类失败");
        }
        return true;
    }

    @Override
    public List<CategoryDO> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryDAO.selectBatchIds(ids.stream().collect(Collectors.toList()));
    }

    @Override
    public CategoryDO getByName(String name) {
        return categoryDAO.selectByName(name);
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
        CategoryDO existing = categoryDAO.selectByName(categoryName);
        if (existing != null && !existing.getId().equals(excludeId)) {
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
        CategoryDO category = categoryDAO.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        }
        return category;
    }

    /**
     * 转换为分类响应对象
     */
    private CategoryDTO convertToCategoryDTO(CategoryDO category) {
        CategoryDTO categoryResponse = new CategoryDTO();
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