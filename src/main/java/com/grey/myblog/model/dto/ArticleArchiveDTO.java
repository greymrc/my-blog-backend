package com.grey.myblog.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 文章归档 DTO（轻量级）
 *
 * @author grey
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleArchiveDTO implements Serializable {
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
    private CategoryDTO category;

    /**
     * 标签列表
     */
    private List<TagDTO> tags;
}