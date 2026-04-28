-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`            bigint          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `account`       varchar(256)    NOT NULL                COMMENT '用户账号',
    `password`      varchar(512)    NOT NULL                COMMENT '用户密码',
    `nickname`      varchar(256)    DEFAULT NULL            COMMENT '用户昵称',
    `email`         varchar(256)    DEFAULT NULL            COMMENT '用户邮箱',
    `mobile`        varchar(20)     DEFAULT NULL            COMMENT '用户手机号码',
    `gender`        tinyint(1)      DEFAULT NULL            COMMENT '用户性别(0女1男)',
    `avatar`        varchar(1024)   DEFAULT NULL            COMMENT '用户头像',
    `profile`       varchar(512)    DEFAULT NULL            COMMENT '用户简介',
    `role`          varchar(256)    NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
    `create_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint         NOT NULL DEFAULT '0'    COMMENT '是否删除（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account` (`account`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    KEY `idx_user_name` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 文章表
CREATE TABLE IF NOT EXISTS `article` (
    `id`            bigint          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_title`         varchar(255)    NOT NULL                COMMENT '文章标题',
    `article_content`       longtext                                COMMENT '文章内容',
    `article_excerpt`       varchar(500)    DEFAULT NULL            COMMENT '文章摘要',
    `cover_image`   varchar(1024)    DEFAULT NULL            COMMENT '封面图片URL',
    `sort_order`    int             NOT NULL DEFAULT '0'    COMMENT '排序权重（0为默认，1000为置顶）',
    `view_count`    int             NOT NULL DEFAULT '0'    COMMENT '阅读量',
    `category_id`   bigint          DEFAULT NULL            COMMENT '所属分类ID',
    `author_id`     bigint          NOT NULL                COMMENT '作者ID（关联用户表）',
    `status`        tinyint         NOT NULL DEFAULT '0'    COMMENT '文章状态（0-草稿，1-公开，2-私密）',
    `create_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint         NOT NULL DEFAULT '0'    COMMENT '逻辑删除标记（0-正常，1-删除）',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_author_id` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 文章分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id`            bigint          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          varchar(50)     NOT NULL                COMMENT '分类名称',
    `sort_order`    int             NOT NULL DEFAULT '0'    COMMENT '排序权重（0为默认，1000为置顶）',
    `create_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint         NOT NULL DEFAULT '0'    COMMENT '逻辑删除标记（0-正常，1-删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
    `id`            bigint          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          varchar(50)     NOT NULL                COMMENT '标签名称',
    `color`         varchar(50)     DEFAULT NULL            COMMENT '标签颜色',
    `create_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint         NOT NULL DEFAULT '0'    COMMENT '逻辑删除标记（0-正常，1-删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 文章-标签关联表
CREATE TABLE IF NOT EXISTS `article_tag` (
    `id`            bigint          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`    bigint          NOT NULL                COMMENT '文章ID',
    `tag_id`        bigint          NOT NULL                COMMENT '标签ID',
    `create_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章-标签关联表';

-- 网站信息表
CREATE TABLE IF NOT EXISTS `website_info` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `blogger_name`   varchar(100)   NOT NULL                COMMENT '博主名称',
    `avatar`         varchar(1024)  DEFAULT NULL            COMMENT '博主头像',
    `intro`          varchar(500)   DEFAULT NULL            COMMENT '博主简介',
    `github_url`     varchar(255)   DEFAULT NULL            COMMENT 'GitHub链接',
    `email`          varchar(255)   DEFAULT NULL            COMMENT '联系邮箱',
    `about_content`  text           DEFAULT NULL            COMMENT '关于我内容',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint        NOT NULL DEFAULT '0'    COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站信息表';
