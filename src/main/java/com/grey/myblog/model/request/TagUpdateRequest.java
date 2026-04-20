package com.grey.myblog.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签更新请求
 *
 * @author grey
 */
@Data
public class TagUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色
     */
    private String color;
}
