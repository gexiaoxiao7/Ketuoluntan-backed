package com.suibe.suibe_mma.exception;

import com.suibe.suibe_mma.enumeration.UserExceptionEnumeration;

public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable e) {
        super(message, e);
    }

    public static UserException getInstance(UserExceptionEnumeration enumeration) {
        return getInstance(enumeration, null);
    }

    public static UserException getInstance(UserExceptionEnumeration enumeration, Throwable e) {
        switch (enumeration) {
            case USER_ACCOUNT_EXISTS:
                return new UserException("该用户已存在", e);
            case USER_ACCOUNT_NOT_EXISTS:
                return new UserException("该用户不存在", e);
            case USER_ACCOUNT_OR_PASSWORD_WRONG:
                return new UserException("账号名或密码错误", e);
            case USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG:
                return new UserException("账号名或密码格式错误", e);
            case USER_INSERT_FAILED:
                return new UserException("用户信息添加失败", e);
            case USER_PASSWORD_NOT_EQUALS_CHECKPASSWORD:
                return new UserException("密码与校验码不一致", e);
            default:
                return new UserException("默认用户异常", e);
        }
    }
}
