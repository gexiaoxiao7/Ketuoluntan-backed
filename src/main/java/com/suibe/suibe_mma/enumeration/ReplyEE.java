package com.suibe.suibe_mma.enumeration;

import com.suibe.suibe_mma.exception.ReplyException;

public enum ReplyEE {
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
    REPLY_TOPIC_REPLYNUM_ADD_FAILED("题目回复数增加失败"),
    /**
     * 回复删除失败
     */
    REPLY_REMOVE_FAILED("回复删除失败"),
    /**
     * 回复作者id不匹配
     */
    REPLY_USERID_MATCH_FALIED("回复作者id不匹配"),
    /**
     * 题目回复数修改失败
     */
    REPLY_TOPIC_REPLYNUM_SUB_FAILED("回复对应题目回复数减少失败"),
    /**
     * 回复作者积分更新失败
     */
    REPLY_USER_SCORE_UPDATE_FAILED("回复作者积分更新失败"),
    /**
     * 回复id列表为空
     */
    REPLY_IDS_IS_NULL("回复id列表为空"),
    /**
     * 回复id列表无效
     */
    REPLY_IDS_IS_WRONG("回复id列表无效");

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 异常信息构造方法
     * @param message 异常信息
     */
    ReplyEE(String message) {
        this.message = message;
    }

    /**
     * 抛出题目异常
     * @throws ReplyException 回复异常
     */
    public void throwE() throws ReplyException {
        throw new ReplyException(message);
    }

    /**
     * 抛出题目异常
     * @param e 来源异常
     * @throws ReplyException 回复异常
     */
    public void throwE(Exception e) throws ReplyException {
        throw new ReplyException(message, e);
    }
}
