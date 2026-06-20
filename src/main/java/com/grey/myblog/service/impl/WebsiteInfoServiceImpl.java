package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.grey.myblog.dao.WebsiteInfoDAO;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.model.dataobject.WebsiteInfoDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.WebsiteInfoUpdateRequest;
import com.grey.myblog.model.response.WebsiteInfoResponse;
import com.grey.myblog.service.WebsiteInfoService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 网站信息服务实现类
 *
 * @author grey
 */
@Service
public class WebsiteInfoServiceImpl implements WebsiteInfoService {

    private static final int MAX_BLOGGER_NAME_LENGTH = 100;
    private static final int MAX_INTRO_LENGTH = 500;

    @Resource
    private WebsiteInfoDAO websiteInfoDAO;

    @Override
    public WebsiteInfoResponse getWebsiteInfo() {
        WebsiteInfoDO websiteInfo = getOrCreateWebsiteInfo();
        return convertToWebsiteInfoResponse(websiteInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWebsiteInfo(WebsiteInfoUpdateRequest request) {
        validateWebsiteInfoUpdateRequest(request);
        WebsiteInfoDO existingWebsiteInfo = getOrCreateWebsiteInfo();

        WebsiteInfoDO websiteInfo = new WebsiteInfoDO();
        websiteInfo.setId(existingWebsiteInfo.getId());
        websiteInfo.setBloggerName(normalizeBloggerName(request.getBloggerName()));
        websiteInfo.setAvatar(normalizeOptionalField(request.getAvatar()));
        websiteInfo.setIntro(normalizeIntro(request.getIntro()));
        websiteInfo.setGithubUrl(normalizeOptionalField(request.getGithubUrl()));
        websiteInfo.setEmail(normalizeOptionalField(request.getEmail()));
        websiteInfo.setAboutContent(normalizeOptionalField(request.getAboutContent()));
        websiteInfo.setUpdateTime(new Date());

        int result = websiteInfoDAO.updateById(websiteInfo);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新网站信息失败");
        }
        return true;
    }

    /**
     * 获取或初始化网站信息
     */
    private WebsiteInfoDO getOrCreateWebsiteInfo() {
        WebsiteInfoDO websiteInfo = websiteInfoDAO.selectFirst();
        if (websiteInfo != null) {
            return websiteInfo;
        }

        WebsiteInfoDO defaultWebsiteInfo = new WebsiteInfoDO();
        defaultWebsiteInfo.setBloggerName("Grey");
        defaultWebsiteInfo.setUpdateTime(new Date());
        defaultWebsiteInfo.setIsDeleted(0);

        int result = websiteInfoDAO.insert(defaultWebsiteInfo);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "初始化网站信息失败");
        }
        return defaultWebsiteInfo;
    }

    /**
     * 校验网站信息更新请求
     */
    private void validateWebsiteInfoUpdateRequest(WebsiteInfoUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        String bloggerName = normalizeBloggerName(request.getBloggerName());
        if (StrUtil.isBlank(bloggerName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "博主名称不能为空");
        }
        if (bloggerName.length() > MAX_BLOGGER_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "博主名称长度不能超过100");
        }

        String intro = normalizeIntro(request.getIntro());
        if (intro != null && intro.length() > MAX_INTRO_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "博主简介长度不能超过500");
        }
    }

    /**
     * 转换为网站信息响应对象
     */
    private WebsiteInfoResponse convertToWebsiteInfoResponse(WebsiteInfoDO websiteInfo) {
        WebsiteInfoResponse response = new WebsiteInfoResponse();
        BeanUtils.copyProperties(websiteInfo, response);
        return response;
    }

    /**
     * 标准化博主名称
     */
    private String normalizeBloggerName(String bloggerName) {
        return StrUtil.trim(bloggerName);
    }

    /**
     * 标准化博主简介
     */
    private String normalizeIntro(String intro) {
        String normalizedIntro = StrUtil.trim(intro);
        return StrUtil.isBlank(normalizedIntro) ? null : normalizedIntro;
    }

    /**
     * 标准化可空字段
     */
    private String normalizeOptionalField(String value) {
        String normalizedValue = StrUtil.trim(value);
        return StrUtil.isBlank(normalizedValue) ? null : normalizedValue;
    }
}