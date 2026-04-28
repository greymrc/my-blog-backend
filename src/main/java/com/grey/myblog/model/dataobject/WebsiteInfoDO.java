package com.grey.myblog.model.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 网站信息表
 *
 * @author grey
 */
@TableName(value = "website_info")
@Data
public class WebsiteInfoDO implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
