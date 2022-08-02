package com.suibe.suibe_mma.exception;

/**
 * 用户相关操作异常
 */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable e) {
        super(message, e);
    }
}
