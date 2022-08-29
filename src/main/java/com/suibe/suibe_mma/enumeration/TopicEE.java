package com.suibe.suibe_mma.enumeration;

import com.suibe.suibe_mma.exception.TopicException;

/**
 * 题目相关操作异常枚举类
 */
public enum TopicEE {
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
    TOPIC_MESSAGE_WRONG("题目信息有误"),
    /**
     * 题目删除失败
     */
    TOPIC_REMOVE_FAILED("题目删除失败"),
    /**
     * 题目作者id不匹配
     */
    TOPIC_USERID_MATCH_FAILED("题目作者id不匹配"),
    /**
     * 相关题目回复删除失败
     */
    TOPIC_REMOVE_REPLY_FAILED("相关题目回复删除失败"),
    /**
     * 题目id列表为空
     */
    TOPIC_IDS_IS_NULL("题目id列表为空"),
    /**
     * 题目id列表无效
     */
    TOPIC_IDS_IS_WRONG("题目id列表无效"),
    /**
     * 无搜索结果
     */
    TOPIC_SEARCH_TITLE_WRONG("无搜索结果"),
    /**
     * 题目信息更新失败
     */
    TOPIC_INFO_UPDATE_FAILED("题目信息更新失败"),
    /**
     * 精选信息更新失败
     */
    TOPIC_IS_STARE_UPDATE_FAILED("精选信息更新失败");

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 异常信息构造方法
     * @param message 异常信息
     */
    TopicEE(String message) {
        this.message = message;
    }

    /**
     * 抛出题目异常
     * @throws TopicException 题目异常
     */
    public void throwE() throws TopicException {
        throw new TopicException(message);
    }

    /**
     * 抛出题目异常
     * @param e 来源异常
     * @throws TopicException 题目异常
     */
    public void throwE(Exception e) throws TopicException {
        throw new TopicException(message, e);
    }
}
