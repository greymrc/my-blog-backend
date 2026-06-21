package com.grey.myblog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.grey.myblog.dao.UserDAO;
import com.grey.myblog.constant.UserConstant;
import com.grey.myblog.exception.AssertUtil;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.model.PageResult;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.enums.UserRoleEnum;
import com.grey.myblog.model.request.UserAddRequest;
import com.grey.myblog.model.request.UserPageListRequest;
import com.grey.myblog.model.request.UserUpdateRequest;
import com.grey.myblog.model.dto.UserDTO;
import com.grey.myblog.service.UserService;
import com.grey.myblog.utils.ValidationUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author grey
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDAO userDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 校验注册参数
        checkRegisterParams(userAccount, userPassword, checkPassword);

        // 校验账号唯一（查表）
        AssertUtil.isFalse(existsByAccount(userAccount), ErrorCode.PARAMS_ERROR, "账户已存在");

        // 密码加密
        String encryptPassword = this.getEncryptPassword(userPassword);

        // 组装用户数据并插入数据库
        UserDO registerUser = UserDO.builder()
                .account(userAccount)
                .password(encryptPassword)
                .role(UserRoleEnum.COMMON_USER.getValue())
                .nickname("未命名")
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        int result = userDAO.insert(registerUser);
        AssertUtil.isTrue(result > 0, ErrorCode.SYSTEM_ERROR, "用户注册失败");

        return registerUser.getId();
    }

    /**
     * 校验用户注册参数
     */
    private void checkRegisterParams(String userAccount, String userPassword, String checkPassword) {
        // 校验参数非空
        AssertUtil.isFalse(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数不能为空");

        // 校验账户长度 4-20
        int accountLength = userAccount.length();
        AssertUtil.isTrue(accountLength >= 4 && accountLength <= 20,
                ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");

        // 校验密码长度 8-20
        int passwordLength = userPassword.length();
        AssertUtil.isTrue(passwordLength >= 8 && passwordLength <= 20,
                ErrorCode.PARAMS_ERROR, "密码长度应为8-20之间");

        // 校验两次密码一致
        AssertUtil.isTrue(userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次密码不一致");
    }

    /**
     * 判断账号是否已存在
     */
    private boolean existsByAccount(String userAccount) {
        return userDAO.countByAccount(userAccount) > 0;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        final String salt = "Ciallo～(∠・ω< )⌒★";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }

    @Override
    public UserDTO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 参数校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
        }
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应为8-20之间");
        }

        // 密码加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 查表对比
        UserDO loginUser = userDAO.selectByAccountAndPassword(userAccount, encryptPassword);
        if (loginUser == null) {
            log.error("用户登录失败，账号或者密码错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        // 用户数据脱敏
        UserDTO loginUserVo = getLoginUserVo(loginUser);
        // 用户登录态保存
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, loginUserVo);
        // 返回用户脱敏信息
        return loginUserVo;
    }

    @Override
    public UserDTO getLoginUserVo(UserDO user) {
        UserDTO loginUserVo = new UserDTO();
        BeanUtils.copyProperties(user, loginUserVo);
        return loginUserVo;
    }

    @Override
    public UserDO getLoginUser(HttpServletRequest request) {
        // 从 session 获取用户对象
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);

        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        if (!(userObj instanceof UserDTO)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录状态异常，请重新登录");
        }

        UserDTO loginUser = (UserDTO) userObj;

        if (loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录信息不完整");
        }

        // 查询用户信息，拿到最新的用户对象
        UserDO latestUser = userDAO.selectById(loginUser.getId());

        if (latestUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在或已被删除");
        }

        return latestUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean userAdd(UserAddRequest userAddRequest) {
        String userAccount = userAddRequest.getAccount();
        String userPassword = userAddRequest.getPassword();
        // 校验用户账号密码合规
        checkUserAccountPassword(userAccount, userPassword);
        // 校验账号唯一
        if (existsByAccount(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已存在");
        }
        // 转换为User类
        UserDO user = UserDO.builder()
                .account(userAddRequest.getAccount())
                .password(getEncryptPassword(userPassword))
                .nickname(StrUtil.isBlank(userAddRequest.getNickname()) ? "未命名" : userAddRequest.getNickname())
                .profile(userAddRequest.getProfile())
                .role(StrUtil.isBlank(userAddRequest.getRole()) ? UserRoleEnum.COMMON_USER.getValue() : userAddRequest.getRole())
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        // 插入到数据库
        try {
            int result = userDAO.insert(user);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户添加失败");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已存在");
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateRequest userUpdateRequest, UserDO loginUser) {
        // 校验参数
        if (userUpdateRequest.getId() <= 0L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id非法");
        }
        String userAccount = userUpdateRequest.getAccount();
        if (!StrUtil.hasBlank(userAccount)) {
            if (userAccount.length() < 4 || userAccount.length() > 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
            }
        }
        // 校验邮箱格式
        String userEmail = userUpdateRequest.getEmail();
        ValidationUtils.validateEmail(userEmail);
        // 校验手机号格式
        String userMobile = userUpdateRequest.getMobile();
        ValidationUtils.validateMobile(userMobile);
        // 转换对象
        UserDO user = UserDO.builder()
                .id(userUpdateRequest.getId())
                .account(userUpdateRequest.getAccount())
                .nickname(userUpdateRequest.getNickname())
                .email(userUpdateRequest.getEmail())
                .mobile(userUpdateRequest.getMobile())
                .avatar(userUpdateRequest.getAvatar())
                .profile(userUpdateRequest.getProfile())
                .role(userUpdateRequest.getRole())
                .updateTime(new Date())
                .build();
        // 更新
        int result = userDAO.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户更新失败");
        }
        return true;
    }

    @Override
    public PageResult<UserDTO> userPageList(UserPageListRequest userPageListRequest) {
        // 参数校验
        long pageNum = userPageListRequest.getPageNum();
        long pageSize = userPageListRequest.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 5;
        }

        try {
            PageHelper.startPage((int) pageNum, (int) pageSize);
            List<UserDO> userList = userDAO.selectUserPage(userPageListRequest);
            PageInfo<UserDO> pageInfo = new PageInfo<>(userList);

            List<UserDTO> userVOList = userList.stream()
                    .map(this::getUserVo)
                    .collect(Collectors.toList());

            return new PageResult<>(pageNum, pageSize, pageInfo.getTotal(), userVOList);
        } catch (Exception e) {
            log.error("分页查询异常：", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "分页查询失败");
        }
    }

    @Override
    public UserDTO getUserVo(UserDO user) {
        if (user == null) {
            return new UserDTO();
        }
        UserDTO userVo = new UserDTO();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public List<UserDO> getUserNameByIds(List<Long> userIds) {
        if (userIds == null) {
            return new ArrayList<>();
        }
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userDAO.selectBatchIds(userIds);
    }

    @Override
    public Boolean isAdmin(UserDO loginUser) {
        String userRole = loginUser.getRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getRoleEnumByValue(userRole);
        return userRoleEnum != null && UserRoleEnum.ADMIN_USER.equals(userRoleEnum);
    }

    @Override
    public List<UserDO> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userDAO.selectBatchIds(ids.stream().collect(Collectors.toList()));
    }

    /**
     * 校验用户的账号密码是否符合规范
     */
    public void checkUserAccountPassword(String userAccount, String userPassword) {
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
        }
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应为8-20之间");
        }
    }
}