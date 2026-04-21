package com.grey.myblog.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签响应对象
 *
 * @author grey
 */
@Data
public class TagResponse implements Serializable {
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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
