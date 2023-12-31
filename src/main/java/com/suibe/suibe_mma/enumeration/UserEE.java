package com.suibe.suibe_mma.enumeration;

import com.suibe.suibe_mma.exception.UserException;

/**
 * 用户相关异常信息枚举类
 */
public enum UserEE {
    /**
     * 用户登录等操作时显示用户不存在
     */
    USER_ACCOUNT_NOT_EXISTS("该用户不存在"),
    /**
     * 用户登录时显示账号名或密码错误
     */
    USER_ACCOUNT_OR_PASSWORD_WRONG("账号或密码错误"),
    /**
     * 用户注册时显示该用户名已存在
     */
    USER_ACCOUNT_EXISTS("该用户已存在"),
    /**
     * 用户注册时显示账户名或密码不合规定
     */
    USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG("账号或密码格式错误"),
    /**
     * 用户注册时添加失败
     */
    USER_INSERT_FAILED("用户信息添加失败"),
    /**
     * 密码与校验码不一致
     */
    USER_PASSWORD_NOT_EQUALS_CHECK_PASSWORD("密码与校验码不一致"),
    /**
     * 获取当前对象时与数据库中的信息不一致
     */
    USER_INFORMATION_WRONG("用户信息不匹配"),
    /**
     * 用户对其昵称等信息更新时失败
     */
    USER_INFO_UPDATE_FAILED("用户信息更新失败"),
    /**
     * 用户id已删除或不存在
     */
    USER_ID_WRONG("用户ID无效"),
    /**
     * 用户积分更新失败
     */
    USER_SCORE_UPDATE_FAILED("用户积分更新失败"),
    /**
     * 用户id为null
     */
    USER_ID_IS_NULL("用户ID为空"),
    /**
     * 旧密码格式不符
     */
    USER_OLD_PASSWORD_FORMAT_WRONG("旧密码格式不符"),
    /**
     * 新密码格式不符
     */
    USER_NEW_PASSWORD_FORMAT_WRONG("新密码格式不符"),
    /**
     * 新密码与旧密码相同
     */
    USER_NEW_AND_OLD_PASSWORD_SAME("新密码与旧密码相同"),
    /**
     * 密码修改失败
     */
    USER_PASSWORD_CHANGE_FAILED("密码修改失败"),
    /**
     * 用户已被封号
     */
    USER_SEALED("用户已被封号"),
    /**
     * 用户封号失败
     */
    USER_SEAL_FAILED("用户封号失败"),
    /**
     * 该用户不为管理员
     */
    USER_NOT_MANAGER("该用户不为管理员"),
    /**
     * 用户解封失败
     */
    USER_UNSEAL_FAILED("用户解封失败"),
    /**
     * 管理员权限修改失败
     */
    USER_MANAGER_ROLE_CHANGE_FAILED("管理员权限修改失败"),
    /**
     * 该用户不是普通用户
     */
    USER_NOT_NORMAL("该用户不是普通用户"),
    /**
     * 用户id列表为空
     */
    USER_IDS_IS_NULL("用户id列表为空"),
    /**
     * 用户id列表无效
     */
    USER_IDS_IS_WRONG("用户id列表无效"),
    /**
     * 该用户未被封
     */
    USER_NOT_SEAL("该用户未被封"),
    /**
     * 旧密码输入有误
     */
    USER_PASSWORD_WRONG("旧密码输入有误");

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 异常信息构造器
     * @param message 异常信息
     */
    UserEE(String message) {
        this.message = message;
    }

    /**
     * 根据枚举类抛出相应的异常信息
     * @throws UserException 用户相关操作异常
     */
    public void throwE() throws UserException {
        throw new UserException(message);
    }

    /**
     * 根据枚举类抛出相应的异常信息
     * @param e 来源异常
     * @throws UserException 用户相关操作异常
     */
    public void throwE(Exception e) throws UserException {
        throw new UserException(message, e);
    }
}
