package com.grey.myblog.model.dataobject;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章-标签关联表
 */
@Data
public class ArticleTagDO implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}