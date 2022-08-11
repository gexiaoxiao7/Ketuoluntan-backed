package com.suibe.suibe_mma.enumeration;

import com.suibe.suibe_mma.exception.UserException;

/**
 * 用户相关异常信息枚举类
 */
public enum  UserExceptionEnumeration {
    /**
     * 用户登录等操作时显示用户不存在
     */
    USER_ACCOUNT_NOT_EXISTS("该用户不存在"),
    /**
     * 用户登录时显示账号名或密码错误
     */
    USER_ACCOUNT_OR_PASSWORD_WRONG("账号名或密码错误"),
    /**
     * 用户注册时显示该用户名已存在
     */
    USER_ACCOUNT_EXISTS("该用户已存在"),
    /**
     * 用户注册时显示账户名或密码不合规定
     */
    USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG("账号名或密码格式错误"),
    /**
     * 用户注册时添加失败
     */
    USER_INSERT_FAILED("用户信息添加失败"),
    /**
     * 用户注册时密码与校验码不一致
     */
    USER_PASSWORD_NOT_EQUALS_CHECK_PASSWORD("密码与校验码不一致"),
    /**
     * 获取当前对象时与数据库中的信息不一致
     */
    USER_INFORMATION_WRONG("用户信息不一致"),
    /**
     * 用户对其昵称等信息更新时失败
     */
    USER_INFO_UPDATE_FAILED("用户信息更新失败"),
    /**
     * 用户id已删除或不存在
     */
    USER_ID_WRONG("用户ID不存在"),
    /**
     * 用户积分更新失败
     */
    USER_SCORE_UPDATE_FAILED("用户积分更新失败"),
    /**
     * 用户id为null
     */
    USER_ID_IS_NULL("用户ID为空");

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 异常信息构造器
     * @param message 异常信息
     */
    UserExceptionEnumeration(String message) {
        this.message = message;
    }

    /**
     * 根据枚举类抛出相应的异常信息
     * @throws UserException 用户相关操作异常
     */
    public void throwUserException() throws UserException {
        throw new UserException(message);
    }

    /**
     * 根据枚举类抛出相应的异常信息
     * @param e 来源异常
     * @throws UserException 用户相关操作异常
     */
    public void throwUserException(Exception e) throws UserException {
        throw new UserException(message, e);
    }
}
