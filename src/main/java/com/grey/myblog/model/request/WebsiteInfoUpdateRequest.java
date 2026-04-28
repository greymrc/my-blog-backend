package com.grey.myblog.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 网站信息更新请求
 *
 * @author grey
 */
@Data
public class WebsiteInfoUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

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
}
