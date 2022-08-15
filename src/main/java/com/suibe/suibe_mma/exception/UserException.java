package com.suibe.suibe_mma.exception;

/**
 * 用户相关操作异常
 */
public class UserException
        extends RuntimeException {
    /**
     * message构造方法
     * @param message 异常信息
     */
    public UserException(String message) {
        super(message);
    }

    /**
     * message、e构造方法
     * @param message 异常信息
     * @param e 来源异常
     */
    public UserException(String message, Throwable e) {
        super(message, e);
    }
}
