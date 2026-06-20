package com.grey.myblog.service;

import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.request.UserAddRequest;
import com.grey.myblog.model.request.UserPageListRequest;
import com.grey.myblog.model.request.UserUpdateRequest;
import com.grey.myblog.model.response.LoginUserResponse;
import com.grey.myblog.model.response.UserResponse;
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
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户主键id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * md5加密
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      HTTP请求
     * @return 登录用户信息
     */
    LoginUserResponse userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 根据传入的user获取到脱敏后的登录用户数据
     *
     * @param user 用户实体
     * @return 登录用户VO
     */
    LoginUserResponse getLoginUserVo(UserDO user);

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求
     * @return 用户实体
     */
    UserDO getLoginUser(HttpServletRequest request);

    /**
     * 当前用户退出登录
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 添加用户
     *
     * @param userAddRequest 添加请求
     * @return 是否成功
     */
    boolean userAdd(UserAddRequest userAddRequest);

    /**
     * 更新用户
     *
     * @param userUpdateRequest 更新请求
     * @param loginUser         登录用户
     * @return 是否成功
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest, UserDO loginUser);

    /**
     * 用户分页查询
     *
     * @param userPageListRequest 分页查询请求
     * @return 分页结果
     */
    PageResult<UserResponse> userPageList(UserPageListRequest userPageListRequest);

    /**
     * 用户User脱敏为UserVo
     *
     * @param user 用户实体
     * @return 用户VO
     */
    UserResponse getUserVo(UserDO user);

    /**
     * 根据用户们的id获取他们的名称，用于图片上传者信息展示
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<UserDO> getUserNameByIds(List<Long> userIds);

    /**
     * 根据传入用户判断是否为管理员
     *
     * @param loginUser 登录用户
     * @return 是否为管理员
     */
    Boolean isAdmin(UserDO loginUser);

    /**
     * 根据ID批量查询用户
     *
     * @param ids 用户ID集合
     * @return 用户列表
     */
    List<UserDO> listByIds(Collection<Long> ids);
}