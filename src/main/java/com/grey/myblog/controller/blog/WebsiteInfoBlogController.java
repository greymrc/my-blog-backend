package com.grey.myblog.controller.blog;

import com.grey.myblog.common.Result;
import com.grey.myblog.model.dto.WebsiteInfoDTO;
import com.grey.myblog.service.WebsiteInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网站信息接口（博客端）
 *
 * @author grey
 */
@RestController
@RequestMapping("/blog/website-info")
public class WebsiteInfoBlogController {

    @Resource
    private WebsiteInfoService websiteInfoService;

    /**
     * 获取网站信息
     */
    @GetMapping("/get")
    public Result<WebsiteInfoDTO> getWebsiteInfo() {
        return Result.success(websiteInfoService.getWebsiteInfo());
    }
}
