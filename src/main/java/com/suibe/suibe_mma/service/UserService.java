package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.domain.User;

public interface UserService extends IService<User> {
    String SALT = "suibe_mma";
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
}
