package com.grey.myblog.controller.app;

import com.grey.myblog.common.Result;
import com.grey.myblog.model.dto.TagDTO;
import com.grey.myblog.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签接口（用户端）
 *
 * @author grey
 */
@RestController
@RequestMapping("/app/tag")
public class TagAppController {

    @Resource
    private TagService tagService;

    /**
     * 获取全部标签列表
     */
    @GetMapping("/all")
    public Result<List<TagDTO>> listAllTags() {
        return Result.success(tagService.listAllTags());
    }
}
