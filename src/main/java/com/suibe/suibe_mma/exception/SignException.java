package com.suibe.suibe_mma.exception;

/**
 * 信息异常类
 */
public class SignException
        extends RuntimeException {

    /**
     * sign构造方法
     * @param message 异常信息
     */
    public SignException(String message) {
        super(message);
    }

    /**
     * message、e构造方法
     * @param message 异常信息
     * @param e 来源异常
     */
    public SignException(String message, Exception e) {
        super(message, e);
    }
}
