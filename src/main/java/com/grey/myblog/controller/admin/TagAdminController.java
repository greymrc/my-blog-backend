package com.grey.myblog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.annotation.AuthCheck;
import com.grey.myblog.common.Result;
import com.grey.myblog.model.DeleteRequest;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.TagAddRequest;
import com.grey.myblog.model.request.TagPageListRequest;
import com.grey.myblog.model.request.TagUpdateRequest;
import com.grey.myblog.model.vo.TagVO;
import com.grey.myblog.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理接口
 *
 * @author grey
 */
@RestController
@RequestMapping("/admin/tag")
public class TagAdminController {

    @Resource
    private TagService tagService;

    /**
     * 标签分页列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = "admin")
    public Result<Page<TagVO>> listTagPage(@RequestBody(required = false) TagPageListRequest request) {
        if (request == null) {
            request = new TagPageListRequest();
        }
        return Result.success(tagService.listTagPage(request));
    }

    /**
     * 查询全部标签
     */
    @GetMapping("/all")
    @AuthCheck(mustRole = "admin")
    public Result<List<TagVO>> listAllTags() {
        return Result.success(tagService.listAllTags());
    }

    /**
     * 根据ID查询标签
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = "admin")
    public Result<TagVO> getTagById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "标签ID非法");
        }
        return Result.success(tagService.getTagById(id));
    }

    /**
     * 新增标签
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public Result<Long> addTag(@RequestBody TagAddRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        return Result.success(tagService.addTag(request));
    }

    /**
     * 修改标签
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> updateTag(@RequestBody TagUpdateRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        return Result.success(tagService.updateTag(request));
    }

    /**
     * 删除标签
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest) {
        if (ObjectUtils.isEmpty(deleteRequest)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        return Result.success(tagService.deleteTag(deleteRequest.getId()));
    }
}
