package com.grey.myblog.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 文章响应对象
 *
 * @author grey
 */
@Data
public class ArticleResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章内容（详情接口返回）
     */
    private String articleContent;

    /**
     * 文章摘要
     */
    private String articleExcerpt;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 阅读量
     */
    private Integer viewCount;

    /**
     * 文章状态（0-草稿，1-公开，2-私密）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 所属分类ID（用于关联查询）
     */
    private Long categoryId;

    /**
     * 作者ID（用于关联查询）
     */
    private Long authorId;

    /**
     * 分类信息
     */
    private CategoryResponse category;

    /**
     * 作者信息（简化版：id、nickname、avatar）
     */
    private AuthorResponse author;

    /**
     * 标签列表
     */
    private List<TagResponse> tags;

    /**
     * 字数统计
     */
    private Integer wordCount;

    /**
     * 作者简化信息
     */
    @Data
    public static class AuthorResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 作者ID
         */
        private Long id;

        /**
         * 作者昵称
         */
        private String nickname;

        /**
         * 作者头像
         */
        private String avatar;
    }
}
