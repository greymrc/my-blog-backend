package com.grey.myblog.model.dataobject;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章分类表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDO implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序权重（0为默认，1000为置顶）
     */
    private Integer sortOrder;

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