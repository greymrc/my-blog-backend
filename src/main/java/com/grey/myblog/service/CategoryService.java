package com.grey.myblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.model.dataobject.CategoryDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.grey.myblog.model.request.CategoryAddRequest;
import com.grey.myblog.model.request.CategoryPageListRequest;
import com.grey.myblog.model.request.CategoryUpdateRequest;
import com.grey.myblog.model.response.CategoryResponse;

import java.util.List;

/**
* @author grey
* @description 针对表【category(文章分类表)】的数据库操作Service
* @createDate 2026-01-15 11:49:53
*/
public interface CategoryService extends IService<CategoryDO> {

    /**
     * 分页查询分类列表
     *
     * @param request 分页查询请求
     * @return 分页分类列表
     */
    Page<CategoryResponse> listCategoryPage(CategoryPageListRequest request);

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
}
