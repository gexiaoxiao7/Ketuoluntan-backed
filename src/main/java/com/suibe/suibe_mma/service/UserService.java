package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.domain.User;

/**
 * 用户服务类接口
 */
public interface UserService extends IService<User> {
    /**
     * 加密盐值
     */
    String SALT = "suibe_mma";

    /**
     * 用户登录状态
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 注册用户
     * @param userRegisterRequest 用户注册信息
     * @throws UserException 格式不符、用户存在、添加失败
     */
    void register(UserRegisterRequest userRegisterRequest) throws UserException;

    /**
     * 用户登录
     * @param userLoginRequest 用户登录信息
     * @return 安全用户信息
     * @throws UserException 格式不符、用户不存在
     */
    User login(UserLoginRequest userLoginRequest) throws UserException;

    /**
     * 获取当前登录用户
     * @param currentUser session域中获取的用户信息
     * @return 用户信息
     * @throws UserException 数据库中无此用户、用户信息不一致
     */
    User checkCurrentUser(User currentUser) throws UserException;

    /**
     * 更新用户信息，除了密码等重要信息
     * @param user 用户需要更新信息
     * @return 用户更新信息后的用户信息
     * @throws UserException 更新失败
     */
    User updateUserInfo(User user) throws UserException;

    /**
     * 根据用户id，增加用户积分
     * @param user 用户信息
     * @param score 用户积分
     * @return 更新后用户信息
     * @throws UserException 增加用户积分失败
     */
    User changeScore(User user, Integer score) throws UserException;
}
