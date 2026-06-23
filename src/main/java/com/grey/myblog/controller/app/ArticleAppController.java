package com.grey.myblog.controller.app;

import com.grey.myblog.common.Result;
import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dto.ArticleArchiveDTO;
import com.grey.myblog.model.dto.ArticleDTO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.ArticlePageListRequest;
import com.grey.myblog.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文章接口（用户端）
 *
 * @author grey
 */
@RestController
@RequestMapping("/app/article")
public class ArticleAppController {

    @Resource
    private ArticleService articleService;

    /**
     * 文章列表（主页）（无正文，只有摘要）
     */
    @PostMapping("/list")
    public Result<PageResult<ArticleDTO>> listArticles(@RequestBody(required = false) ArticlePageListRequest request) {
        if (request == null) {
            request = new ArticlePageListRequest();
        }
        PageResult<ArticleDTO> articlePage = articleService.listArticles(request);
        return Result.success(articlePage);
    }

    /**
     * 获取文章详情
     * 返回完整文章内容，包含正文、分类、作者、标签等关联信息
     * 访问时自动增加阅读量
     */
    @GetMapping("/{id}")
    public Result<ArticleDTO> getArticleById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        ArticleDTO articleVO = articleService.getArticleById(id);
        return Result.success(articleVO);
    }

    /**
     * 获取文章归档列表（轻量级）
     * 按年月分组返回文章列表，支持按年份或年月筛选
     * 只返回必要字段：id、articleTitle、createTime、category、tags
     * 返回格式：Map<年份, Map<月份, List<文章归档VO>>>
     */
    @GetMapping("/archive")
    public Result<Map<String, Map<String, List<ArticleArchiveDTO>>>> getArticleArchive(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Map<String, Map<String, List<ArticleArchiveDTO>>> archiveMap = articleService.getArticleArchive(year, month);
        return Result.success(archiveMap);
    }
}
