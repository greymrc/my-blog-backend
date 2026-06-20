package com.grey.myblog.model.dataobject;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章表
 */
@Data
public class ArticleDO implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章内容
     */
    private String articleContent;

    /**
     * 文章摘要
     */
    private String articleExcerpt;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 排序权重（0为默认，1000为置顶）
     */
    private Integer sortOrder;

    /**
     * 阅读量
     */
    private Integer viewCount;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 作者ID（关联用户表）
     */
    private Long authorId;

    /**
     * 文章状态（0-草稿，1-公开，2-私密）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除标记（0-正常，1-删除）
     */
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}