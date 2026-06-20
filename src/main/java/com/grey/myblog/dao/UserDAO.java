package com.grey.myblog.dao;

import com.grey.myblog.model.dataobject.UserDO;
import com.grey.myblog.model.request.UserPageListRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 DAO
 *
 * @author grey
 */
public interface UserDAO {

    /**
     * 插入用户
     */
    int insert(UserDO user);

    /**
     * 批量插入用户
     */
    int insertBatch(@Param("list") List<UserDO> users);

    /**
     * 根据ID更新用户
     */
    int updateById(UserDO user);

    /**
     * 根据ID逻辑删除用户
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询用户
     */
    UserDO selectById(@Param("id") Long id);

    /**
     * 根据ID批量查询用户
     */
    List<UserDO> selectBatchIds(@Param("ids") List<Long> ids);

    /**
     * 根据账号查询用户
     */
    UserDO selectByAccount(@Param("account") String account);

    /**
     * 查询用户列表
     */
    List<UserDO> selectList(@Param("user") UserDO user);

    /**
     * 查询用户总数
     */
    long selectCount(@Param("user") UserDO user);

    /**
     * 分页查询用户列表
     */
    List<UserDO> selectUserPage(@Param("request") UserPageListRequest request);

    /**
     * 根据账号和密码查询用户（登录）
     */
    UserDO selectByAccountAndPassword(@Param("account") String account, @Param("password") String password);

    /**
     * 根据账号统计数量（检查账号是否存在）
     */
    long countByAccount(@Param("account") String account);
}