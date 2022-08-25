package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.request.UserChangePasswordRequest;
import com.suibe.suibe_mma.domain.request.UserIdRequest;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.domain.User;

/**
 * 用户服务类接口
 */
public interface UserService
        extends IService<User> {
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
     * 根据用户id，改变用户积分
     * @param user 用户信息
     * @param score 用户积分
     * @return 更新后用户信息
     * @throws UserException 改变用户积分失败
     */
    User changeScore(
            User user,
            Integer score) throws UserException;

    /**
     * 修改密码
     * @param request 修改密码信息类
     * @param currentUser 当前登录用户
     * @return 用户信息
     * @throws UserException 新旧密码格式错误或一致，新密码与校验码不一致，用户id无效或为空，更新密码失败
     */
    User changePassword(
            UserChangePasswordRequest request,
            User currentUser) throws UserException;

    /**
     * 封号用户
     * @param userIdRequest 用户id类
     * @param currentUser 当前登录用户
     * @throws UserException 该用户已被封，id无效或为空，封号失败
     */
    void sealUser(
            UserIdRequest userIdRequest,
            User currentUser) throws UserException;

    /**
     * 解封用户
     * @param userIdRequest 用户id类
     * @param currentUser 当前登录用户
     * @throws UserException 该用户未被封，id无效或为空，解封失败
     */
    void unsealUser(
            UserIdRequest userIdRequest,
            User currentUser) throws UserException;

    /**
     * 给普通用户修改权限
     * @param user 用户信息
     * @throws UserException 修改失败
     */
    void giveManager(User user) throws UserException;

    /**
     * 取消管理员权限
     * @param user 用户信息
     * @throws UserException 修改失败
     */
    void recaptureManager(User user) throws UserException;

    /**
     * 月积分重置
     * @throws UserException 月积分更新失败
     */
    void monthScoreReset() throws UserException;
}
