package com.grey.myblog.controller.admin;

import com.grey.myblog.annotation.AuthCheck;
import com.grey.myblog.common.Result;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.WebsiteInfoUpdateRequest;
import com.grey.myblog.model.response.WebsiteInfoResponse;
import com.grey.myblog.service.WebsiteInfoService;
import jakarta.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网站信息管理接口
 *
 * @author grey
 */
@RestController
@RequestMapping("/admin/website-info")
public class WebsiteInfoAdminController {

    @Resource
    private WebsiteInfoService websiteInfoService;

    /**
     * 获取网站信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = "admin")
    public Result<WebsiteInfoResponse> getWebsiteInfo() {
        return Result.success(websiteInfoService.getWebsiteInfo());
    }

    /**
     * 更新网站信息
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> updateWebsiteInfo(@RequestBody WebsiteInfoUpdateRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        return Result.success(websiteInfoService.updateWebsiteInfo(request));
    }
}
