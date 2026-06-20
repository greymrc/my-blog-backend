package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.grey.myblog.dao.TagDAO;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.model.PageResult;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 *
 * @author grey
 */
@Service
public class TagServiceImpl implements TagService {

    private static final int MAX_TAG_NAME_LENGTH = 50;

    @Resource
    private TagDAO tagDAO;

    @Resource
    private ArticleTagService articleTagService;

    @Override
    public PageResult<TagResponse> listTagPage(TagPageListRequest request) {
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

        PageHelper.startPage((int) pageNum, (int) pageSize);
        List<TagDO> tagList = tagDAO.selectTagPage(request);
        PageInfo<TagDO> pageInfo = new PageInfo<>(tagList);

        List<TagResponse> tagVOList = tagList.stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());

        return new PageResult<>(pageNum, pageSize, pageInfo.getTotal(), tagVOList);
    }

    @Override
    public List<TagResponse> listAllTags() {
        TagDO query = new TagDO();
        List<TagDO> tagList = tagDAO.selectList(query);
        return tagList.stream()
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

        int result = tagDAO.insert(tag);
        if (result <= 0) {
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

        int result = tagDAO.updateById(tag);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新标签失败");
        }
        return true;
    }

    @Override
    public Boolean deleteTag(Long id) {
        TagDO existingTag = getExistingTag(id);
        long relationCount = articleTagService.countByTagId(existingTag.getId());
        if (relationCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "标签已被文章使用，暂不允许删除");
        }

        int result = tagDAO.deleteById(existingTag.getId());
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除标签失败");
        }
        return true;
    }

    @Override
    public List<TagDO> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return tagDAO.selectBatchIds(ids.stream().collect(Collectors.toList()));
    }

    @Override
    public TagDO getByName(String name) {
        return tagDAO.selectByName(name);
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
        TagDO existing = tagDAO.selectByName(tagName);
        if (existing != null && !existing.getId().equals(excludeId)) {
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
        TagDO tag = tagDAO.selectById(id);
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