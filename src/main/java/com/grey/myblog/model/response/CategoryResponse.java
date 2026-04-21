package com.grey.myblog.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类响应对象
 *
 * @author grey
 */
@Data
public class CategoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
