package com.grey.myblog.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建文章请求
 *
 * @author grey
 */
@Data
public class ArticleAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文章标题（必填）
     */
    private String title;

    /**
     * 文章内容（必填）
     */
    private String content;

    /**
     * 文章摘要（可选）
     */
    private String excerpt;

    /**
     * 封面图片URL（可选）
     */
    private String coverImage;

    /**
     * 所属分类ID（可选）
     */
    private Long categoryId;

    /**
     * 标签ID数组（可选）
     */
    private List<Long> tagIds;

    /**
     * 文章状态（0-草稿，1-公开，2-私密，默认0）
     */
    private Integer status = 0;

    /**
     * 排序权重（默认0）
     */
    private Integer sortOrder = 0;
}
