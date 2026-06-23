package com.grey.myblog.controller.blog;

import com.grey.myblog.common.Result;
import com.grey.myblog.model.dto.CategoryDTO;
import com.grey.myblog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口（博客端）
 *
 * @author grey
 */
@RestController
@RequestMapping("/blog/category")
public class CategoryBlogController {

    @Resource
    private CategoryService categoryService;

    /**
     * 获取全部分类列表
     */
    @GetMapping("/all")
    public Result<List<CategoryDTO>> listAllCategories() {
        return Result.success(categoryService.listAllCategories());
    }
}
