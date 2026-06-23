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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        log.info("action=register, account={}, result=processing", userAccount);

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

        log.info("action=register, account={}, userId={}, role={}, result=success", userAccount, registerUser.getId(), registerUser.getRole());
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
        log.info("action=login, account={}, result=processing", userAccount);

        // 参数校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            log.warn("action=login, account={}, reason=params_empty, result=fail", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            log.warn("action=login, account={}, reason=account_length_invalid, result=fail", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
        }
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            log.warn("action=login, account={}, reason=password_length_invalid, result=fail", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应为8-20之间");
        }

        // 密码加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 查表对比
        UserDO loginUser = userDAO.selectByAccountAndPassword(userAccount, encryptPassword);
        if (loginUser == null) {
            log.warn("action=login, account={}, reason=account_or_password_error, result=fail", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        // 生成 token: admin-UUID (去掉横线)
        String token = "admin-" + UUID.randomUUID().toString().replace("-", "");

        // 用户数据脱敏
        UserDTO loginUserVo = getLoginUserVo(loginUser);
        // 设置 token
        loginUserVo.setToken(token);

        // 将用户信息存入 Redis，key 为 token
        String redisKey = UserConstant.TOKEN_KEY_PREFIX + token;
        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(loginUser.getId()),
                UserConstant.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        log.info("action=login, account={}, userId={}, role={}, result=success", userAccount, loginUser.getId(), loginUser.getRole());
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
        // 从请求头获取 token
        String token = request.getHeader(UserConstant.TOKEN_HEADER_KEY);
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        // 从 Redis 获取用户 ID
        String redisKey = UserConstant.TOKEN_KEY_PREFIX + token;
        String userIdStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isBlank(userIdStr)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录或登录已过期");
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录状态异常，请重新登录");
        }

        // 查询用户信息，拿到最新的用户对象
        UserDO latestUser = userDAO.selectById(userId);
        if (latestUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在或已被删除");
        }

        return latestUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 从请求头获取 token
        String token = request.getHeader(UserConstant.TOKEN_HEADER_KEY);
        if (StrUtil.isBlank(token)) {
            log.warn("action=logout, reason=not_login, result=fail");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }

        // 从 Redis 删除 token
        String redisKey = UserConstant.TOKEN_KEY_PREFIX + token;
        stringRedisTemplate.delete(redisKey);

        log.info("action=logout, result=success");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean userAdd(UserAddRequest userAddRequest) {
        String userAccount = userAddRequest.getAccount();
        log.info("action=add_user, account={}, result=processing", userAccount);

        String userPassword = userAddRequest.getPassword();
        // 校验用户账号密码合规
        checkUserAccountPassword(userAccount, userPassword);
        // 校验账号唯一
        if (existsByAccount(userAccount)) {
            log.warn("action=add_user, account={}, reason=account_exists, result=fail", userAccount);
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
                log.error("action=add_user, account={}, reason=db_insert_fail, result=fail", userAccount);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户添加失败");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("action=add_user, account={}, reason=account_duplicate, result=fail", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已存在");
        }

        log.info("action=add_user, account={}, userId={}, role={}, result=success", userAccount, user.getId(), user.getRole());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateRequest userUpdateRequest, UserDO loginUser) {
        Long targetUserId = userUpdateRequest.getId();
        log.info("action=update_user, operatorId={}, operatorAccount={}, targetUserId={}, result=processing",
                loginUser.getId(), loginUser.getAccount(), targetUserId);

        // 校验参数
        if (targetUserId <= 0L) {
            log.warn("action=update_user, targetUserId={}, reason=id_invalid, result=fail", targetUserId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id非法");
        }
        String userAccount = userUpdateRequest.getAccount();
        if (!StrUtil.hasBlank(userAccount)) {
            if (userAccount.length() < 4 || userAccount.length() > 20) {
                log.warn("action=update_user, account={}, reason=account_length_invalid, result=fail", userAccount);
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
                .id(targetUserId)
                .account(userUpdateRequest.getAccount())
                .nickname(userUpdateRequest.getNickname())
                .email(userEmail)
                .mobile(userMobile)
                .avatar(userUpdateRequest.getAvatar())
                .profile(userUpdateRequest.getProfile())
                .role(userUpdateRequest.getRole())
                .updateTime(new Date())
                .build();
        // 更新
        int result = userDAO.updateById(user);
        if (result <= 0) {
            log.error("action=update_user, targetUserId={}, reason=db_update_fail, result=fail", targetUserId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户更新失败");
        }

        log.info("action=update_user, targetUserId={}, nickname={}, email={}, role={}, result=success",
                targetUserId, userUpdateRequest.getNickname(), userEmail, userUpdateRequest.getRole());
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