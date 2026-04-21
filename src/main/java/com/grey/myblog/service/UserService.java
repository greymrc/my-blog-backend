package com.grey.myblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grey.myblog.model.dataobject.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.grey.myblog.model.request.UserAddRequest;
import com.grey.myblog.model.request.UserPageListRequest;
import com.grey.myblog.model.request.UserUpdateRequest;
import com.grey.myblog.model.response.LoginUserResponse;
import com.grey.myblog.model.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author grey
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-01-14 13:11:18
*/
public interface UserService extends IService<UserDO> {

    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户主键id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * md5加密
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    LoginUserResponse userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 根据传入的user获取到脱敏后的登录用户数据
     * @param user
     * @return
     */
    LoginUserResponse getLoginUserVo(UserDO user);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    UserDO getLoginUser(HttpServletRequest request);

    /**
     * 当前用户退出登录
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 添加用户
     *
     * @param userAddRequest
     * @return
     */
    boolean userAdd(UserAddRequest userAddRequest);

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param
     * @return
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest, UserDO loginUser);

    /**
     * 用户分页查询
     *
     * @param userPageListRequest
     * @return
     */
    Page<UserResponse> userPageList(UserPageListRequest userPageListRequest);

    /**
     * 用户User脱敏为UserVo
     * @param user
     * @return
     */
    UserResponse getUserVo(UserDO user);


    /**
     * 根据用户们的id获取他们的名称，用于图片上传者信息展示
     *
     * @param userIds
     * @return
     */
    List<UserDO> getUserNameByIds(List<Long> userIds);


    /**
     * 根据传入用户判断是否为管理员
     * @param loginUser
     * @return
     */
    Boolean isAdmin(UserDO loginUser);
}
