package com.grey.myblog.model.dataobject;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网站信息表
 *
 * @author grey
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteInfoDO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 博主名称
     */
    private String bloggerName;

    /**
     * 博主头像
     */
    private String avatar;

    /**
     * 博主简介
     */
    private String intro;

    /**
     * GitHub链接
     */
    private String githubUrl;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 关于我内容
     */
    private String aboutContent;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}