package com.suibe.suibe_mma.enumeration;

import com.suibe.suibe_mma.exception.TopicException;

public enum TopicExceptionEnumeration {
    TOPIC_TITLE_IS_SPACE("题目标题为空"),
    TOPIC_USR_ID_IS_NULL("用户id为空"),
    TOPIC_USER_ID_IS_WRONG("用户id不存在"),
    TOPIC_INSERT_FAILED("添加题目失败");

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 异常信息构造方法
     * @param message 异常信息
     */
    TopicExceptionEnumeration(String message) {
        this.message = message;
    }

    /**
     * 抛出题目异常
     * @throws TopicException 题目异常
     */
    public void throwTopicException() throws TopicException {
        throw new TopicException(message);
    }

    /**
     * 抛出题目异常
     * @param e 来源异常
     * @throws TopicException 题目异常
     */
    public void throwTopicException(Exception e) throws TopicException {
        throw new TopicException(message, e);
    }
}
