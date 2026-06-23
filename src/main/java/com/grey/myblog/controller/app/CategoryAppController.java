package com.grey.myblog.controller.app;

import com.grey.myblog.common.Result;
import com.grey.myblog.model.dto.CategoryDTO;
import com.grey.myblog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口（用户端）
 *
 * @author grey
 */
@RestController
@RequestMapping("/app/category")
public class CategoryAppController {

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
