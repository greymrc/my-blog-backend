package com.grey.myblog.constant;


/**
 * 用户模块所需常量
 * @author grey
 */
public interface UserConstant {

    /**
     * 用户session信息存储的Key（已废弃，改用 Token）
     */
    String USER_LOGIN_STATUS="user_login_status";

    // region
    /**
     * 普通用户角色常量
     */
    String USER ="user";

    /**
     * 管理员角色常量
     */
    String ADMIN ="admin";

    /**
     * Token 存储的 Redis Key 前缀
     */
    String TOKEN_KEY_PREFIX = "token:";

    /**
     * Token 过期时间（30天，单位：秒）
     */
    long TOKEN_EXPIRE_TIME = 30 * 24 * 60 * 60L;

    /**
     * 请求头中 Token 的 key
     */
    String TOKEN_HEADER_KEY = "token";

}
