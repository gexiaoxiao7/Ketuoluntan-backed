package com.suibe.suibe_mma.enumeration;


import com.suibe.suibe_mma.exception.MessageException;

public enum MessageEE {
    /**
     * 信息唯一标识为空
     */
    MESSAGE_ID_NULL("信息id为空"),
    /**
     * 信息id无效
     */
    MESSAGE_ID_WRONG("信息id无效"),
    /**
     * 信息id列表为空
     */
    MESSAGE_IDS_NULL("信息id列表为空"),
    /**
     * 信息id列表无效
     */
    MESSAGE_IDS_WRONG("信息id列表无效"),
    /**
     * 信息读取状态改变失败
     */
    MESSAGE_READ_UPDATE_FAILED("信息读取状态改变失败"),
    /**
     * 发送者不是本人
     */
    MESSAGE_NOT_CURRENT("发送者不是本人"),
    /**
     * 信息添加失败
     */
    MESSAGE_SAVE_FAILED("信息添加失败"),
    /**
     * 信息内容为空
     */
    MESSAGE_CONTENT_EMPTY("信息内容为空"),
    /**
     * 信息已被删除
     */
    MESSAGE_DELETE("信息已被删除"),
    /**
     * 信息无法被读取
     */
    MESSAGE_UNABLE_READ("信息无法被读取"),
    /**
     * 信息删除失败
     */
    MESSAGE_DELETE_FAILED("信息删除失败"),
    /**
     * 发送id与当前用户不符
     */
    MESSAGE_SEND_ID_NOT_EQUAL("发送id与当前用户不符"),
    /**
     * 不能发送信息给自己
     */
    MESSAGE_CAN_NOT_SAME("不能发送信息给自己");

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 异常信息构造方法
     * @param message 异常信息
     */
    MessageEE(String message) {
        this.message = message;
    }

    /**
     * 抛出题目异常
     * @throws MessageException 回复异常
     */
    public void throwE() throws MessageException {
        throw new MessageException(message);
    }

    /**
     * 抛出题目异常
     * @param e 来源异常
     * @throws MessageException 回复异常
     */
    public void throwE(Exception e) throws MessageException {
        throw new MessageException(message, e);
    }
}
