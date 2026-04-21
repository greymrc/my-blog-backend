package com.grey.myblog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grey.myblog.model.dataobject.ArticleTagDO;

/**
* @author grey
* @description 针对表【article_tag(文章-标签关联表)】的数据库操作DAO
* @createDate 2026-01-15 11:49:42
* @Entity com.grey.myblog.model.dataobject.ArticleTagDO
*/
public interface ArticleTagDAO extends BaseMapper<ArticleTagDO> {

}
