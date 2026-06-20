package com.grey.myblog.dao;

import com.grey.myblog.model.dataobject.WebsiteInfoDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 网站信息表 DAO
 *
 * @author grey
 */
public interface WebsiteInfoDAO {

    /**
     * 插入网站信息
     */
    int insert(WebsiteInfoDO websiteInfo);

    /**
     * 根据ID更新网站信息
     */
    int updateById(WebsiteInfoDO websiteInfo);

    /**
     * 根据ID逻辑删除网站信息
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询网站信息
     */
    WebsiteInfoDO selectById(@Param("id") Long id);

    /**
     * 查询第一条网站信息（用于获取唯一配置）
     */
    WebsiteInfoDO selectFirst();

    /**
     * 查询网站信息列表
     */
    List<WebsiteInfoDO> selectList(@Param("websiteInfo") WebsiteInfoDO websiteInfo);
}