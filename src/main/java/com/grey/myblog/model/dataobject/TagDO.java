package com.grey.myblog.model.dataobject;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDO implements Serializable {
    /**
     * 主键ID
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

    /**
     * 逻辑删除标记（0-正常，1-删除）
     */
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}