package com.grey.myblog.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 文章 DTO
 *
 * @author grey
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章内容（详情接口返回）
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
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 阅读量
     */
    private Integer viewCount;

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
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 分类信息
     */
    private CategoryDTO category;

    /**
     * 作者信息
     */
    private AuthorDTO author;

    /**
     * 标签列表
     */
    private List<TagDTO> tags;

    /**
     * 字数统计
     */
    private Integer wordCount;
}