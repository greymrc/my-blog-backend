package com.grey.myblog.service.impl;

import com.grey.myblog.dao.ArticleTagDAO;
import com.grey.myblog.model.dataobject.ArticleTagDO;
import com.grey.myblog.service.ArticleTagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章-标签关联服务实现类
 *
 * @author grey
 */
@Service
public class ArticleTagServiceImpl implements ArticleTagService {

    @Resource
    private ArticleTagDAO articleTagDAO;

    @Override
    public int save(ArticleTagDO articleTag) {
        return articleTagDAO.insert(articleTag);
    }

    @Override
    public int saveBatch(List<ArticleTagDO> articleTags) {
        return articleTagDAO.insertBatch(articleTags);
    }

    @Override
    public int removeByArticleId(Long articleId) {
        return articleTagDAO.deleteByArticleId(articleId);
    }

    @Override
    public int removeByTagId(Long tagId) {
        return articleTagDAO.deleteByTagId(tagId);
    }

    @Override
    public List<ArticleTagDO> listByArticleId(Long articleId) {
        return articleTagDAO.selectByArticleId(articleId);
    }

    @Override
    public List<ArticleTagDO> listByTagId(Long tagId) {
        return articleTagDAO.selectByTagId(tagId);
    }

    @Override
    public List<ArticleTagDO> listByArticleIds(List<Long> articleIds) {
        return articleTagDAO.selectByArticleIds(articleIds);
    }

    @Override
    public long countByTagId(Long tagId) {
        return articleTagDAO.countByTagId(tagId);
    }
}