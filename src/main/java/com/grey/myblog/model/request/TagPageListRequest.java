package com.grey.myblog.model.request;

import com.grey.myblog.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 标签分页查询请求
 *
 * @author grey
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TagPageListRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标签名称
     */
    private String name;
}
