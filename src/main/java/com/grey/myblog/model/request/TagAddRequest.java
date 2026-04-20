package com.grey.myblog.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签新增请求
 *
 * @author grey
 */
@Data
public class TagAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色
     */
    private String color;
}
