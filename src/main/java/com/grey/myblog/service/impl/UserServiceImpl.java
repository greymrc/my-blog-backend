package com.grey.myblog.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grey.myblog.dao.UserDAO;
import com.grey.myblog.constant.UserConstant;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.enums.UserRoleEnum;
import com.grey.myblog.model.request.UserAddRequest;
import com.grey.myblog.model.request.UserPageListRequest;
import com.grey.myblog.model.request.UserUpdateRequest;
import com.grey.myblog.model.response.LoginUserResponse;
import com.grey.myblog.model.response.UserResponse;
import com.grey.myblog.service.UserService;
import com.grey.myblog.utils.ValidationUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author grey
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-01-14 13:11:18
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserDAO, UserDO>
    implements UserService{


    @Resource
    private UserDAO userDAO;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //校验参数不为空
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        //校验账户长度大于等于4小于等于20
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
        }
        //校验密码长度大于等于8 小于等于20
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应为8-20之间");
        }
        //校验密码和确认密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        //校验账号唯一   （查表）  这里因为account数据库唯一索引，所以没有并发异常，不然这里需要锁来限制同时插入两条一样的。
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getAccount, userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已存在");
        }
        //密码加密
        String encryptPassword = this.getEncryptPassword(userPassword);
        //组装用户注册数据 插入数据库
        UserDO registerUser = new UserDO();
        registerUser.setAccount(userAccount);
        registerUser.setPassword(encryptPassword);
        registerUser.setRole(UserRoleEnum.COMMON_USER.getValue());
        registerUser.setNickname("未命名");   //默认未命名就是用户默认昵称
        //TODO 可以给用户设置默认头像
        //registerUser.setAvatar("");
        boolean saveResult = this.save(registerUser);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
        }
        //返回用户主键
        return registerUser.getId();
    }


    @Override
    public String getEncryptPassword(String userPassword) {
        final String salt = "Ciallo～(∠・ω< )⌒★";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }

    @Override
    public LoginUserResponse userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        /**
         * 1. 参数校验：
         *    1. 非空，长度
         * 2. 密码加密
         * 3. 查表对比
         *    1. 为空抛异常
         * 4. 用户数据脱敏
         * 5. 用户登录态保存
         * 6. 返回用户脱敏信息
         */
        //参数校验：
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        //校验账户长度大于等于4小于等于20
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
        }
        //校验密码长度大于等于8 小于等于20
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应为8-20之间");
        }

        //密码加密
        userPassword = getEncryptPassword(userPassword);

        //查表对比
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getAccount, userAccount);
        queryWrapper.lambda().eq(UserDO::getPassword, userPassword);
        UserDO loginUser = this.getOne(queryWrapper);
        //为空抛异常
        if (loginUser == null) {
            log.error("用户登录失败，账号或者密码错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        //用户数据脱敏
        LoginUserResponse loginUserVo = getLoginUserVo(loginUser);
        //用户登录态保存
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, loginUserVo);
        //返回用户脱敏信息
        return loginUserVo;
    }

    @Override
    public LoginUserResponse getLoginUserVo(UserDO user) {
        //用户数据脱敏
        LoginUserResponse loginUserVo = new LoginUserResponse();
        BeanUtils.copyProperties(user, loginUserVo);
        return loginUserVo;
    }

    @Override
    public UserDO getLoginUser(HttpServletRequest request) {
        /**
         * 1. 从 session 获取登录用户对象
         * 2. 检查是否为 null
         * 3. 检查类型是否正确（防止类型转换异常）
         * 4. 安全地进行类型转换
         * 5. 检查关键字段（id）是否完整
         * 6. 查询最新用户信息，防止缓存与数据库不一致
         * 7. 返回用户对象
         */
        // 从 session 获取用户对象
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        
        // 先检查是否为 null
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        
        // 检查类型是否正确，防止类型转换异常
        if (!(userObj instanceof LoginUserResponse)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录状态异常，请重新登录");
        }
        
        // 安全地进行类型转换
        LoginUserResponse loginUser = (LoginUserResponse) userObj;
        
        // 检查关键字段是否完整
        if (loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录信息不完整");
        }
        
        // 查询用户信息，拿到最新的用户对象，防止缓存跟数据不一致
        UserDO latestUser = this.getById(loginUser.getId());
        
        // 判断是否为空（可能被管理员删除或封禁）
        if (latestUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在或已被删除");
        }
        
        // 返回用户对象
        return latestUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request){
        /**
         * 1. 从请求体session里获取当前用户，不需要转换
         * 2. 判断是否为空
         *    1. 为空抛出异常
         * 3. session移除当前用户。
         */
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        if (userObj==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
        return true;
    }


    @Override
    public boolean userAdd(UserAddRequest userAddRequest) {
        /**
         * 入参： userAddRequest
         *
         *  	1. 校验必须字段非空
         *  	2. 校验字段格式
         *  	3. 转换为User类
         *  	4. 插入到数据库
         *  	5. 判断是否插入成功
         *
         * 出参：添加成功的用户id
         */
        String userAccount = userAddRequest.getAccount();
        String userPassword = userAddRequest.getPassword();
        //校验用户账号密码合规
        checkUserAccountPassword(userAccount, userPassword);
        //校验账号唯一   （查表）
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getAccount, userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已存在");
        }
        //转换为User类
        UserDO user = new UserDO();
        BeanUtils.copyProperties(userAddRequest, user);
        user.setPassword(getEncryptPassword(userPassword));
        if (StrUtil.isBlank(user.getRole())){
            //如果用户角色未设置，那么设置为普通用户
            user.setRole(UserRoleEnum.COMMON_USER.getValue());
        }
        if (StrUtil.isBlank(user.getNickname())){
            user.setNickname("未命名");
        }

        //插入到数据库
        try {
            boolean result = this.save(user);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户添加失败");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 捕获唯一索引冲突异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已存在");
        }

        return true;
    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest, UserDO loginUser) {
        /**
         * 入参：UserUpdateRequest
         * 1.校验参数
         *    1. 非空
         *    2. 有效性
         * 2. 转换对象   ）
         * 3. 更新    （在这之前是否应该校验下参数的有效性？算了暂时不考虑吧）
         * 4. 判断是否更新成功
         * 出参：boolean是否成功
         */
        //校验参数
        if (userUpdateRequest.getId() <= 0L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id非法");
        }
        String userAccount = userUpdateRequest.getAccount();
        //校验用户账号合规
        //不为空校验参数
        if (!StrUtil.hasBlank(userAccount)) {
            //校验账户长度大于等于4小于等于20
            if (userAccount.length() < 4 || userAccount.length() > 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
            }
        }
        //校验邮箱格式
        String userEmail = userUpdateRequest.getEmail();
        ValidationUtils.validateEmail(userEmail);
        //校验手机号格式
        String userMobile = userUpdateRequest.getMobile();
        ValidationUtils.validateMobile(userMobile);
        //转换对象
        UserDO user = new UserDO();
        BeanUtils.copyProperties(userUpdateRequest, user);
        //更新
        boolean result = this.updateById(user);
        //判断是否更新成功
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户更新失败");
        }
        return result;
    }

    /**
     * 分页查询用户
     *
     * @param userPageListRequest
     * @return
     */
    @Override
    public Page<UserResponse> userPageList(UserPageListRequest userPageListRequest) {
        /**
         * 入参： UserPageListRequest
         * 1. 参数校验
         *    1. 非空
         *    2. 有效
         * 2. 对查询参数进行组装
         * 3. 进行分页查询
         * 4. 对分页查询结果进行数据脱敏
         * 5. 返回脱敏后的分页查询结果
         * 出参： 分页后参数page<UserVo>
         */
        //参数校验
        long pageNum = userPageListRequest.getPageNum();
        long pageSize = userPageListRequest.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 5;
        }
        Page<UserResponse> userVoPage = null;
        try {
            //对查询参数进行组装
            QueryWrapper<UserDO> queryWrapper = getQueryWrapper(userPageListRequest);
            //进行分页查询
            Page<UserDO> userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
            //对分页查询结果进行数据脱敏
            List<UserResponse> userVOList = userPage.getRecords().stream().map(this::getUserVo).collect(Collectors.toList());
            //返回脱敏后的分页查询结果
            userVoPage = new Page<>(pageNum,pageSize,userPage.getTotal());
            //保存脱敏后的分页数据
            userVoPage.setRecords(userVOList);
        } catch (Exception e) {
            log.error("分页查询异常：",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"分页查询失败");
        }

        return userVoPage;
    }

    /**
     * 根据传入的User进行数据脱敏为UserVo
     * @param user
     * @return
     */
    @Override
    public UserResponse getUserVo(UserDO user) {
        //非空校验
        if (user==null){
            return new UserResponse();
        }
        UserResponse userVo = new UserResponse();
        BeanUtils.copyProperties(user,userVo);
        return userVo;
    }

    @Override
    public List<UserDO> getUserNameByIds(List<Long> userIds) {
        if (userIds == null) {
            return new ArrayList<>();
        }
        //批量查询
        return userDAO.selectList(
                new LambdaQueryWrapper<UserDO>()
                        .select(UserDO::getId, UserDO::getNickname) // 只查 id 和 username
                        .in(UserDO::getId, userIds)
        );
    }

    @Override
    public Boolean isAdmin(UserDO loginUser) {
        String userRole = loginUser.getRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getRoleEnumByValue(userRole);
        return userRoleEnum != null && UserRoleEnum.ADMIN_USER.equals(userRoleEnum);
    }

    /**
     * 根据传入的userPageListRequest，组装分页查询条件QueryWrapper
     * @param userPageListRequest
     * @return
     */
    public QueryWrapper<UserDO> getQueryWrapper(UserPageListRequest userPageListRequest) {
        if (userPageListRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        Long id = userPageListRequest.getId();
        String userAccount = userPageListRequest.getAccount();
        String userNickname = userPageListRequest.getNickname();
        String userProfile = userPageListRequest.getProfile();
        String userEmail = userPageListRequest.getEmail();
        String userRole = userPageListRequest.getRole();
        String sortField = userPageListRequest.getSortField();
        String sortOrder = userPageListRequest.getSortOrder();

        queryWrapper.lambda().eq(ObjectUtil.isNotNull(id),UserDO::getId,id);
        queryWrapper.lambda().eq(StrUtil.isNotBlank(userAccount),UserDO::getAccount,userAccount);
        queryWrapper.lambda().eq(StrUtil.isNotBlank(userEmail),UserDO::getEmail,userEmail);
        queryWrapper.lambda().eq(StrUtil.isNotBlank(userRole),UserDO::getRole,userRole);
        queryWrapper.lambda().like(StrUtil.isNotBlank(userNickname),UserDO::getNickname,userNickname);
        queryWrapper.lambda().like(StrUtil.isNotBlank(userProfile),UserDO::getProfile,userProfile);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), "descend".equals(sortOrder),sortField);
        return queryWrapper;
    }


    /**
     * 校验用户的账号密码是否符合规范。
     * 账号长度4-20  密码长度4-20
     *
     * @param userAccount
     * @param userPassword
     */
    public void checkUserAccountPassword(String userAccount, String userPassword) {
        //校验参数不为空
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        //校验账户长度大于等于4小于等于20
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度应为4-20之间");
        }
        //校验密码长度大于等于8 小于等于20
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应为8-20之间");
        }
    }

}



