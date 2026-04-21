package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grey.myblog.dao.TagDAO;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.model.dataobject.ArticleTagDO;
import com.grey.myblog.model.dataobject.TagDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.TagAddRequest;
import com.grey.myblog.model.request.TagPageListRequest;
import com.grey.myblog.model.request.TagUpdateRequest;
import com.grey.myblog.model.response.TagResponse;
import com.grey.myblog.service.ArticleTagService;
import com.grey.myblog.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author grey
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2026-01-15 11:49:57
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagDAO, TagDO>
    implements TagService{

    private static final int MAX_TAG_NAME_LENGTH = 50;

    @Resource
    private ArticleTagService articleTagService;

    @Override
    public Page<TagResponse> listTagPage(TagPageListRequest request) {
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

        QueryWrapper<TagDO> queryWrapper = buildQueryWrapper(request);
        Page<TagDO> tagPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<TagResponse> tagVOList = tagPage.getRecords().stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());

        Page<TagResponse> tagVOPage = new Page<>(pageNum, pageSize, tagPage.getTotal());
        tagVOPage.setRecords(tagVOList);
        return tagVOPage;
    }

    @Override
    public List<TagResponse> listAllTags() {
        return this.list(new LambdaQueryWrapper<TagDO>()
                        .orderByAsc(TagDO::getName))
                .stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagById(Long id) {
        TagDO tag = getExistingTag(id);
        return convertToTagResponse(tag);
    }

    @Override
    public Long addTag(TagAddRequest request) {
        validateTagAddRequest(request);
        String tagName = normalizeTagName(request.getName());
        checkTagNameUnique(tagName, null);

        TagDO tag = new TagDO();
        tag.setName(tagName);
        tag.setColor(normalizeColor(request.getColor()));
        tag.setCreateTime(new Date());
        tag.setUpdateTime(new Date());

        boolean result = this.save(tag);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增标签失败");
        }
        return tag.getId();
    }

    @Override
    public Boolean updateTag(TagUpdateRequest request) {
        validateTagUpdateRequest(request);
        TagDO existingTag = getExistingTag(request.getId());
        String tagName = normalizeTagName(request.getName());
        checkTagNameUnique(tagName, existingTag.getId());

        TagDO tag = new TagDO();
        tag.setId(existingTag.getId());
        tag.setName(tagName);
        tag.setColor(normalizeColor(request.getColor()));
        tag.setUpdateTime(new Date());

        boolean result = this.updateById(tag);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新标签失败");
        }
        return true;
    }

    @Override
    public Boolean deleteTag(Long id) {
        TagDO existingTag = getExistingTag(id);
        long relationCount = articleTagService.count(
                new LambdaQueryWrapper<ArticleTagDO>()
                        .eq(ArticleTagDO::getTagId, existingTag.getId())
        );
        if (relationCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "标签已被文章使用，暂不允许删除");
        }

        boolean result = this.removeById(existingTag.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除标签失败");
        }
        return true;
    }

    /**
     * 构建分页查询条件
     */
    private QueryWrapper<TagDO> buildQueryWrapper(TagPageListRequest request) {
        QueryWrapper<TagDO> queryWrapper = new QueryWrapper<>();
        String tagName = StrUtil.trim(request.getName());
        queryWrapper.lambda().like(StrUtil.isNotBlank(tagName), TagDO::getName, tagName);

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder) || "asc".equalsIgnoreCase(sortOrder);

        if (StrUtil.isBlank(sortField)) {
            queryWrapper.lambda().orderByDesc(TagDO::getUpdateTime);
            return queryWrapper;
        }

        switch (sortField) {
            case "name" -> queryWrapper.orderBy(true, isAsc, "name");
            case "createTime", "create_time" -> queryWrapper.orderBy(true, isAsc, "create_time");
            case "updateTime", "update_time" -> queryWrapper.orderBy(true, isAsc, "update_time");
            default -> queryWrapper.lambda().orderByDesc(TagDO::getUpdateTime);
        }
        return queryWrapper;
    }

    /**
     * 校验标签新增请求
     */
    private void validateTagAddRequest(TagAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        validateTagName(request.getName());
    }

    /**
     * 校验标签更新请求
     */
    private void validateTagUpdateRequest(TagUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        if (request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签ID非法");
        }
        validateTagName(request.getName());
    }

    /**
     * 校验标签名称
     */
    private void validateTagName(String tagName) {
        String normalizedTagName = normalizeTagName(tagName);
        if (StrUtil.isBlank(normalizedTagName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称不能为空");
        }
        if (normalizedTagName.length() > MAX_TAG_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称长度不能超过50");
        }
    }

    /**
     * 校验标签名称唯一
     */
    private void checkTagNameUnique(String tagName, Long excludeId) {
        LambdaQueryWrapper<TagDO> queryWrapper = new LambdaQueryWrapper<TagDO>()
                .eq(TagDO::getName, tagName);
        queryWrapper.ne(excludeId != null, TagDO::getId, excludeId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称已存在");
        }
    }

    /**
     * 查询存在的标签
     */
    private TagDO getExistingTag(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签ID非法");
        }
        TagDO tag = this.getById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "标签不存在");
        }
        return tag;
    }

    /**
     * 转换为标签视图对象
     */
    private TagResponse convertToTagResponse(TagDO tag) {
        TagResponse tagVO = new TagResponse();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }

    /**
     * 标准化标签名称
     */
    private String normalizeTagName(String tagName) {
        return StrUtil.trim(tagName);
    }

    /**
     * 标准化颜色字段
     */
    private String normalizeColor(String color) {
        String normalizedColor = StrUtil.trim(color);
        return StrUtil.isBlank(normalizedColor) ? null : normalizedColor;
    }
}


