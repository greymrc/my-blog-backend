package com.grey.myblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.model.dataobject.TagDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.grey.myblog.model.request.TagAddRequest;
import com.grey.myblog.model.request.TagPageListRequest;
import com.grey.myblog.model.request.TagUpdateRequest;
import com.grey.myblog.model.vo.TagVO;

import java.util.List;

/**
* @author grey
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2026-01-15 11:49:57
*/
public interface TagService extends IService<TagDO> {

    /**
     * 分页查询标签列表
     *
     * @param request 分页查询请求
     * @return 分页标签列表
     */
    Page<TagVO> listTagPage(TagPageListRequest request);

    /**
     * 查询全部标签
     *
     * @return 标签列表
     */
    List<TagVO> listAllTags();

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签详情
     */
    TagVO getTagById(Long id);

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
}
