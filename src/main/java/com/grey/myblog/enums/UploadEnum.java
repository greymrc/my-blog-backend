package com.grey.myblog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 文件上传类型枚举
 *
 * @author grey
 */
@Getter
@AllArgsConstructor
public enum UploadEnum {

    /**
     * 文章封面
     */
    ARTICLE_COVER("article/cover/", "文章封面", List.of(".jpg", ".jpeg", ".png", ".gif", ".webp"), 0.5),

    /**
     * 文章内容图片
     */
    ARTICLE_IMAGE("article/image/", "文章图片", List.of(".jpg", ".jpeg", ".png", ".gif", ".webp"), 3.0),

    /**
     * 用户头像
     */
    USER_AVATAR("user/avatar/", "用户头像", List.of(".jpg", ".jpeg", ".png", ".gif", ".webp"), 0.3),

    /**
     * 网站信息头像
     */
    WEBSITE_AVATAR("website/avatar/", "网站头像", List.of(".jpg", ".jpeg", ".png", ".gif", ".webp"), 0.3);

    /**
     * 上传目录
     */
    private final String dir;

    /**
     * 描述
     */
    private final String description;

    /**
     * 支持的格式
     */
    private final List<String> format;

    /**
     * 文件最大大小（单位：MB）
     */
    private final Double limitSize;
}
