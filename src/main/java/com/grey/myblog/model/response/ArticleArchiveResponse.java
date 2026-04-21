package com.grey.myblog.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 文章归档响应对象（轻量级）
 * 用于归档页面展示，只包含必要字段
 *
 * @author grey
 */
@Data
public class ArticleArchiveResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 分类信息
     */
    private CategoryResponse category;

    /**
     * 标签列表（只包含标签名称）
     */
    private List<TagResponse> tags;
}
