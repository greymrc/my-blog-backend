package com.grey.myblog.service;

import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.TagDO;
import com.grey.myblog.model.dto.TagDTO;
import com.grey.myblog.model.request.TagAddRequest;
import com.grey.myblog.model.request.TagPageListRequest;
import com.grey.myblog.model.request.TagUpdateRequest;

import java.util.Collection;
import java.util.List;

/**
 * 标签服务接口
 *
 * @author grey
 */
public interface TagService {

    /**
     * 分页查询标签列表
     */
    PageResult<TagDTO> listTagPage(TagPageListRequest request);

    /**
     * 查询全部标签
     */
    List<TagDTO> listAllTags();

    /**
     * 根据ID查询标签
     */
    TagDTO getTagById(Long id);

    /**
     * 新增标签
     */
    Long addTag(TagAddRequest request);

    /**
     * 更新标签
     */
    Boolean updateTag(TagUpdateRequest request);

    /**
     * 删除标签
     */
    Boolean deleteTag(Long id);

    /**
     * 根据ID批量查询标签
     */
    List<TagDO> listByIds(Collection<Long> ids);

    /**
     * 根据名称查询标签
     */
    TagDO getByName(String name);
}