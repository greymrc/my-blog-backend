package com.grey.myblog.service;

import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.TagDO;
import com.grey.myblog.model.request.TagAddRequest;
import com.grey.myblog.model.request.TagPageListRequest;
import com.grey.myblog.model.request.TagUpdateRequest;
import com.grey.myblog.model.response.TagResponse;

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
     *
     * @param request 分页查询请求
     * @return 分页标签列表
     */
    PageResult<TagResponse> listTagPage(TagPageListRequest request);

    /**
     * 查询全部标签
     *
     * @return 标签列表
     */
    List<TagResponse> listAllTags();

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签详情
     */
    TagResponse getTagById(Long id);

    /**
     * 新增标签
     *
     * @param request 新增请求
     * @return 标签ID
     */
    Long addTag(TagAddRequest request);

    /**
     * 更新标签
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateTag(TagUpdateRequest request);

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 是否成功
     */
    Boolean deleteTag(Long id);

    /**
     * 根据ID批量查询标签
     *
     * @param ids 标签ID集合
     * @return 标签列表
     */
    List<TagDO> listByIds(Collection<Long> ids);

    /**
     * 根据名称查询标签
     *
     * @param name 标签名称
     * @return 标签实体
     */
    TagDO getByName(String name);
}