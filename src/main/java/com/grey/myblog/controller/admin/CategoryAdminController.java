package com.grey.myblog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.annotation.AuthCheck;
import com.grey.myblog.common.Result;
import com.grey.myblog.model.DeleteRequest;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.CategoryAddRequest;
import com.grey.myblog.model.request.CategoryPageListRequest;
import com.grey.myblog.model.request.CategoryUpdateRequest;
import com.grey.myblog.model.response.CategoryResponse;
import com.grey.myblog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理接口
 *
 * @author grey
 */
@RestController
@RequestMapping("/admin/category")
public class CategoryAdminController {

    @Resource
    private CategoryService categoryService;

    /**
     * 分类分页列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = "admin")
    public Result<Page<CategoryResponse>> listCategoryPage(@RequestBody(required = false) CategoryPageListRequest request) {
        if (request == null) {
            request = new CategoryPageListRequest();
        }
        return Result.success(categoryService.listCategoryPage(request));
    }

    /**
     * 查询全部分类
     */
    @GetMapping("/all")
    @AuthCheck(mustRole = "admin")
    public Result<List<CategoryResponse>> listAllCategories() {
        return Result.success(categoryService.listAllCategories());
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = "admin")
    public Result<CategoryResponse> getCategoryById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "分类ID非法");
        }
        return Result.success(categoryService.getCategoryById(id));
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public Result<Long> addCategory(@RequestBody CategoryAddRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        return Result.success(categoryService.addCategory(request));
    }

    /**
     * 修改分类
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> updateCategory(@RequestBody CategoryUpdateRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        return Result.success(categoryService.updateCategory(request));
    }

    /**
     * 删除分类
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> deleteCategory(@RequestBody DeleteRequest deleteRequest) {
        if (ObjectUtils.isEmpty(deleteRequest)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        return Result.success(categoryService.deleteCategory(deleteRequest.getId()));
    }
}
