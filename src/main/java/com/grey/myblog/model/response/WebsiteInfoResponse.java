package com.grey.myblog.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 网站信息响应对象
 *
 * @author grey
 */
@Data
public class WebsiteInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

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
}
