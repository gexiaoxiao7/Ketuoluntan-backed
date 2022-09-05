package com.suibe.suibe_mma.exception;

/**
 * 信息异常类
 */
public class MessageException
        extends RuntimeException {

    /**
     * message构造方法
     * @param message 异常信息
     */
    public MessageException(String message) {
        super(message);
    }

    /**
     * message、e构造方法
     * @param message 异常信息
     * @param e 来源异常
     */
    public MessageException(String message, Exception e) {
        super(message, e);
    }
}
