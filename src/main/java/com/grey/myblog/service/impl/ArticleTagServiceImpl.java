package com.grey.myblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grey.myblog.dao.ArticleTagDAO;
import com.grey.myblog.model.dataobject.ArticleTagDO;
import com.grey.myblog.service.ArticleTagService;
import org.springframework.stereotype.Service;

/**
 * @author grey
 * @description 针对表【article_tag(文章-标签关联表)】的数据库操作Service实现
 * @createDate 2026-01-15 11:49:42
 */
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagDAO, ArticleTagDO>
        implements ArticleTagService {

}



