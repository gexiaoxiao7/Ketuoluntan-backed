package com.suibe.suibe_mma.exception;

/**
 * 题目异常信息类
 */
public class TopicException extends RuntimeException {
    /**
     * message构造方法
     * @param message 异常信息
     */
    public TopicException(String message) {
        super(message);
    }

    /**
     * message、e构造方法
     * @param message 异常信息
     * @param e 来源异常
     */
    public TopicException(String message, Exception e) {
        super(message, e);
    }
}
