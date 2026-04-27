package com.grey.myblog.model.request;

import com.grey.myblog.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分类分页查询请求
 *
 * @author grey
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryPageListRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String name;
}
