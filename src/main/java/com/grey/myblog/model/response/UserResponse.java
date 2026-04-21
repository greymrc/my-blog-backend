package com.grey.myblog.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户响应对象
 *
 * @author grey
 */
@Data
public class UserResponse implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户账号
     */
    private String account;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户性别(0女1男)
     */
    private Integer gender;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 用户角色：user/admin
     */
    private String role;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
