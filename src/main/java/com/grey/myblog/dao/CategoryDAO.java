package com.grey.myblog.dao;

import com.grey.myblog.model.dataobject.CategoryDO;
import com.grey.myblog.model.request.CategoryPageListRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类表 DAO
 *
 * @author grey
 */
public interface CategoryDAO {

    /**
     * 插入分类
     */
    int insert(CategoryDO category);

    /**
     * 批量插入分类
     */
    int insertBatch(@Param("list") List<CategoryDO> categories);

    /**
     * 根据ID更新分类
     */
    int updateById(CategoryDO category);

    /**
     * 根据ID逻辑删除分类
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询分类
     */
    CategoryDO selectById(@Param("id") Long id);

    /**
     * 根据ID批量查询分类
     */
    List<CategoryDO> selectBatchIds(@Param("ids") List<Long> ids);

    /**
     * 查询分类列表
     */
    List<CategoryDO> selectList(@Param("category") CategoryDO category);

    /**
     * 查询分类总数
     */
    long selectCount(@Param("category") CategoryDO category);

    /**
     * 根据名称查询分类
     */
    CategoryDO selectByName(@Param("name") String name);

    /**
     * 分页查询分类列表
     */
    List<CategoryDO> selectCategoryPage(@Param("request") CategoryPageListRequest request);

    /**
     * 查询分类下的文章数量
     */
    long countArticlesByCategoryId(@Param("categoryId") Long categoryId);
}