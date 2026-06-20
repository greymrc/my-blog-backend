package com.grey.myblog.service;

import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.CategoryDO;
import com.grey.myblog.model.request.CategoryAddRequest;
import com.grey.myblog.model.request.CategoryPageListRequest;
import com.grey.myblog.model.request.CategoryUpdateRequest;
import com.grey.myblog.model.response.CategoryResponse;

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
     *
     * @param request 分页查询请求
     * @return 分页分类列表
     */
    PageResult<CategoryResponse> listCategoryPage(CategoryPageListRequest request);

    /**
     * 查询全部分类
     *
     * @return 分类列表
     */
    List<CategoryResponse> listAllCategories();

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 新增分类
     *
     * @param request 新增请求
     * @return 分类ID
     */
    Long addCategory(CategoryAddRequest request);

    /**
     * 更新分类
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateCategory(CategoryUpdateRequest request);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    Boolean deleteCategory(Long id);

    /**
     * 根据ID批量查询分类
     *
     * @param ids 分类ID集合
     * @return 分类列表
     */
    List<CategoryDO> listByIds(Collection<Long> ids);

    /**
     * 根据名称查询分类
     *
     * @param name 分类名称
     * @return 分类实体
     */
    CategoryDO getByName(String name);
}