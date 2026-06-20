package com.grey.myblog.service;

import com.grey.myblog.model.request.WebsiteInfoUpdateRequest;
import com.grey.myblog.model.response.WebsiteInfoResponse;

/**
 * 网站信息服务接口
 *
 * @author grey
 */
public interface WebsiteInfoService {

    /**
     * 获取网站信息
     *
     * @return 网站信息
     */
    WebsiteInfoResponse getWebsiteInfo();

    /**
     * 更新网站信息
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateWebsiteInfo(WebsiteInfoUpdateRequest request);
}