package com.grey.myblog.controller.admin;

import cn.hutool.core.util.ObjUtil;
import com.grey.myblog.annotation.AuthCheck;
import com.grey.myblog.common.Result;
import com.grey.myblog.exception.ThrowUtil;
import com.grey.myblog.model.DeleteRequest;
import com.grey.myblog.model.entity.User;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.ArticleAddRequest;
import com.grey.myblog.model.request.ArticlePageListRequest;
import com.grey.myblog.model.request.ArticleUpdateRequest;
import com.grey.myblog.model.vo.ArticleVO;
import com.grey.myblog.model.vo.ArticleArchiveVO;
import com.grey.myblog.service.ArticleService;
import com.grey.myblog.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文章接口
 *
 * @author grey
 */
@RestController
@RequestMapping("/app/article")
public class ArticleAdminController {

    @Resource
    private ArticleService articleService;

    @Resource
    private UserService userService;

    /**
     * 文章列表（主页）（无正文，只有摘要）
     */
    @PostMapping("/list")
    public Result<Page<ArticleVO>> listArticles(@RequestBody ArticlePageListRequest request) {
        // 请求参数为空时使用默认值，避免空指针异常
        if (request == null) {
            request = new ArticlePageListRequest();
        }
        Page<ArticleVO> articlePage = articleService.listArticles(request);
        return Result.success(articlePage);
    }

    /**
     * 获取文章详情
     * 返回完整文章内容，包含正文、分类、作者、标签等关联信息
     * 访问时自动增加阅读量
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        ArticleVO articleVO = articleService.getArticleById(id);
        return Result.success(articleVO);
    }

    /**
     * 获取文章归档列表（轻量级）
     * 按年月分组返回文章列表，支持按年份或年月筛选
     * 只返回必要字段：id、title、createTime、category、tags
     * 返回格式：Map<年份, Map<月份, List<文章归档VO>>>z
     */
    @GetMapping("/archive")
    public Result<Map<String, Map<String, List<ArticleArchiveVO>>>> getArticleArchive(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Map<String, Map<String, List<ArticleArchiveVO>>> archiveMap = articleService.getArticleArchive(year, month);
        return Result.success(archiveMap);
    }

    /**
     * 创建文章（管理员）
     * 校验参数和登录状态，创建文章并保存标签关联关系
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public Result<Long> addArticle(@RequestBody ArticleAddRequest request, HttpServletRequest httpRequest) {
        if (ObjectUtils.isEmpty(request)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtil.throwIf(ObjUtil.isEmpty(loginUser), ErrorCode.NOT_LOGIN_ERROR, "当前未登录");
        
        Long articleId = articleService.addArticle(request, loginUser);
        return Result.success(articleId);
    }

    /**
     * 更新文章（管理员）
     * 校验参数和权限，更新文章内容并重新保存标签关联关系
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> updateArticle(@RequestBody ArticleUpdateRequest request, HttpServletRequest httpRequest) {
        if (ObjectUtils.isEmpty(request)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtil.throwIf(ObjUtil.isEmpty(loginUser), ErrorCode.NOT_LOGIN_ERROR, "当前未登录");
        
        Boolean result = articleService.updateArticle(request, loginUser);
        return Result.success(result);
    }

    /**
     * 删除文章（管理员）
     * 校验参数和权限，执行逻辑删除文章
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpRequest) {
        if (ObjectUtils.isEmpty(deleteRequest)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtil.throwIf(ObjUtil.isEmpty(loginUser), ErrorCode.NOT_LOGIN_ERROR, "当前未登录");
        
        Boolean result = articleService.deleteArticle(deleteRequest.getId(), loginUser);
        return Result.success(result);
    }

}
