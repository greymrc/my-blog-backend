package com.grey.myblog.service;

import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.dto.UserDTO;
import com.grey.myblog.model.request.UserAddRequest;
import com.grey.myblog.model.request.UserPageListRequest;
import com.grey.myblog.model.request.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.List;

/**
 * 用户服务接口
 *
 * @author grey
 */
public interface UserService {

    /**
     * 用户注册
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码加密
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     */
    UserDTO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取登录用户 DTO
     */
    UserDTO getLoginUserVo(UserDO user);

    /**
     * 获取当前登录用户
     */
    UserDO getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 添加用户
     */
    boolean userAdd(UserAddRequest userAddRequest);

    /**
     * 更新用户
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest, UserDO loginUser);

    /**
     * 用户分页查询
     */
    PageResult<UserDTO> userPageList(UserPageListRequest userPageListRequest);

    /**
     * 用户脱敏
     */
    UserDTO getUserVo(UserDO user);

    /**
     * 根据用户ID列表获取用户名称
     */
    List<UserDO> getUserNameByIds(List<Long> userIds);

    /**
     * 判断是否为管理员
     */
    Boolean isAdmin(UserDO loginUser);

    /**
     * 根据ID批量查询用户
     */
    List<UserDO> listByIds(Collection<Long> ids);
}