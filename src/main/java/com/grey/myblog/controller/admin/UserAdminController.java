package com.grey.myblog.controller.admin;


import cn.hutool.core.util.ObjUtil;
import com.grey.myblog.common.Result;
import com.grey.myblog.exception.BusinessException;
import com.grey.myblog.exception.ThrowUtil;
import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.enums.ErrorCode;
import com.grey.myblog.model.request.UserLoginRequest;
import com.grey.myblog.model.request.UserRegisterRequest;
import com.grey.myblog.model.request.UserUpdateRequest;
import com.grey.myblog.model.vo.LoginUserVO;
import com.grey.myblog.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 *
 * @author grey
 */
@RestController
@RequestMapping("/admin/user")
public class UserAdminController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (ObjectUtils.isEmpty(userRegisterRequest)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "注册体为空");
        }
        String userAccount = userRegisterRequest.getAccount();
        String userPassword = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long userId = userService.userRegister(userAccount, userPassword, checkPassword);
        return Result.success(userId);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (ObjectUtils.isEmpty(userLoginRequest) || ObjectUtils.isEmpty(request) ) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "登录请求体为空");
        }
        String userAccount = userLoginRequest.getAccount();
        String userPassword = userLoginRequest.getPassword();
        LoginUserVO loginUserVo = userService.userLogin(userAccount, userPassword, request);
        return Result.success(loginUserVo);
    }


    /**
     * 用户更新
     */
    @PostMapping("/update")
    public Result<Boolean> userUpdate(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {

        //校验非空
        if (ObjectUtils.isEmpty(userUpdateRequest)) {
            return Result.fail(ErrorCode.PARAMS_ERROR);
        }
        //获取当前登录用户
        UserDO loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(ObjUtil.isEmpty(loginUser),ErrorCode.NOT_LOGIN_ERROR,"当前未登录");


        //进行更新
        boolean result = userService.updateUser(userUpdateRequest,loginUser);
        return Result.success(result);
    }


    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/getLoginUser")
    public Result<LoginUserVO> getLoginUser(HttpServletRequest request) {
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserDO loginUser = userService.getLoginUser(request);
        return Result.success(userService.getLoginUserVo(loginUser));
    }

    /**
     * 用户登出
     */
    @GetMapping("/logout")
    public Result<Boolean> userLogout(HttpServletRequest request){
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return Result.success(result);
    }


}
