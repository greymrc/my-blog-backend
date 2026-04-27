package com.grey.myblog.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类新增请求
 *
 * @author grey
 */
@Data
public class CategoryAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
