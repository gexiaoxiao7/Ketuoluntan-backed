package com.suibe.suibe_mma.enumeration;

import com.suibe.suibe_mma.exception.ReplyException;

public enum ReplyExceptionEnumeration {
    /**
     * 回复的内容为null或""
     */
    REPLY_CONTENT_IS_EMPTY("回复内容为空"),
    /**
     * 回复添加失败
     */
    REPLY_SAVE_FAILED("回复添加失败"),
    /**
     * 回复id为空
     */
    REPLY_ID_IS_NULL("回复id为空"),
    /**
     * 回复id无效
     */
    REPLY_ID_IS_WRONG("回复id无效"),
    /**
     * 回复点赞信息更新失败
     */
    REPLY_LIKE_UPDATE_FAILED("回复点赞信息更新失败"),
    /**
     * 回复信息有误
     */
    REPLY_MESSAGE_WRONG("回复信息有误"),
    /**
     * 题目回复数修改失败
     */
    REPLY_TOPIC_REPLYNUM_ADD_FAILED("题目回复数修改失败");

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 异常信息构造方法
     * @param message 异常信息
     */
    ReplyExceptionEnumeration(String message) {
        this.message = message;
    }

    /**
     * 抛出题目异常
     * @throws ReplyException 回复异常
     */
    public void throwReplyException() throws ReplyException {
        throw new ReplyException(message);
    }

    /**
     * 抛出题目异常
     * @param e 来源异常
     * @throws ReplyException 回复异常
     */
    public void throwTopicException(Exception e) throws ReplyException {
        throw new ReplyException(message, e);
    }
}
