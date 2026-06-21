package com.grey.myblog.service;

import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.CategoryDO;
import com.grey.myblog.model.dto.CategoryDTO;
import com.grey.myblog.model.request.CategoryAddRequest;
import com.grey.myblog.model.request.CategoryPageListRequest;
import com.grey.myblog.model.request.CategoryUpdateRequest;

import java.util.Collection;
import java.util.List;

/**
 * 分类服务接口
 *
 * @author grey
 */
public interface CategoryService {

    /**
     * 分页查询分类列表
     */
    PageResult<CategoryDTO> listCategoryPage(CategoryPageListRequest request);

    /**
     * 查询全部分类
     */
    List<CategoryDTO> listAllCategories();

    /**
     * 根据ID查询分类
     */
    CategoryDTO getCategoryById(Long id);

    /**
     * 新增分类
     */
    Long addCategory(CategoryAddRequest request);

    /**
     * 更新分类
     */
    Boolean updateCategory(CategoryUpdateRequest request);

    /**
     * 删除分类
     */
    Boolean deleteCategory(Long id);

    /**
     * 根据ID批量查询分类
     */
    List<CategoryDO> listByIds(Collection<Long> ids);

    /**
     * 根据名称查询分类
     */
    CategoryDO getByName(String name);
}