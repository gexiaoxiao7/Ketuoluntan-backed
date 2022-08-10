package com.suibe.suibe_mma.exception;

/**
 * 回复异常类
 */
public class ReplyException extends RuntimeException {
    /**
     * message构造方法
     * @param message 异常信息
     */
    public ReplyException(String message) {
        super(message);
    }

    /**
     * message、e构造方法
     * @param message 异常信息
     * @param e 来源异常
     */
    public ReplyException(String message, Exception e) {
        super(message, e);
    }
}
