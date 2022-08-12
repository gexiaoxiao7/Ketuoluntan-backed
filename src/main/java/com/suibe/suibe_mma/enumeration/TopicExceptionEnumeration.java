package com.suibe.suibe_mma.enumeration;

import com.suibe.suibe_mma.exception.TopicException;

/**
 * 题目相关操作异常枚举类
 */
public enum TopicExceptionEnumeration {
    /**
     * 题目标题为""或null
     */
    TOPIC_TITLE_IS_SPACE("题目标题为空"),
    /**
     * 添加题目时失败
     */
    TOPIC_INSERT_FAILED("添加题目失败"),
    /**
     * 点赞等操作时获取的题目id为null
     */
    TOPIC_ID_IS_NULL("题目id为空"),
    /**
     * 点赞等操作时获取的题目id不存在或已被删除
     */
    TOPIC_ID_IS_WRONG("题目id无效"),
    /**
     * 点赞更新时失败
     */
    TOPIC_LIKE_UPDATE_FAILED("题目点赞信息更改失败"),
    /**
     * 题目信息有误
     */
    TOPIC_MESSAGE_WRONG("题目信息有误");

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
