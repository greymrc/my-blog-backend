package com.grey.myblog.dao;

import com.grey.myblog.model.dataobject.TagDO;
import com.grey.myblog.model.request.TagPageListRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签表 DAO
 *
 * @author grey
 */
public interface TagDAO {

    /**
     * 插入标签
     */
    int insert(TagDO tag);

    /**
     * 批量插入标签
     */
    int insertBatch(@Param("list") List<TagDO> tags);

    /**
     * 根据ID更新标签
     */
    int updateById(TagDO tag);

    /**
     * 根据ID逻辑删除标签
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询标签
     */
    TagDO selectById(@Param("id") Long id);

    /**
     * 根据ID批量查询标签
     */
    List<TagDO> selectBatchIds(@Param("ids") List<Long> ids);

    /**
     * 查询标签列表
     */
    List<TagDO> selectList(@Param("tag") TagDO tag);

    /**
     * 查询标签总数
     */
    long selectCount(@Param("tag") TagDO tag);

    /**
     * 根据名称查询标签
     */
    TagDO selectByName(@Param("name") String name);

    /**
     * 分页查询标签列表
     */
    List<TagDO> selectTagPage(@Param("request") TagPageListRequest request);
}