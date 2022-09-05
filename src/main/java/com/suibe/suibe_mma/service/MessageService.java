package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Message;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.MessageIdRequest;
import com.suibe.suibe_mma.domain.request.MessageReportRequest;
import com.suibe.suibe_mma.domain.request.MessageWriteRequest;
import com.suibe.suibe_mma.exception.MessageException;

import java.util.List;

/**
 * 信息服务类
 */
public interface MessageService
        extends IService<Message> {

    /**
     * 获取当前用户接收的所有信息
     * @param current 当前用户信息
     * @return 信息列表
     */
    List<Message> getReceiveMessages(User current);

    /**
     * 获取当前用户发送的所有信息
     * @param current 当前用户信息
     * @return 信息列表
     */
    List<Message> getSendMessages(User current);

    /**
     * 获取信息具体内容
     * @param messageIdRequest 信息唯一标识请求类
     * @param current 当前用户信息
     * @return 信息内容
     * @throws MessageException id为空或无效，更新读取状态失败
     */
    Message read(
            MessageIdRequest messageIdRequest,
            User current) throws MessageException;

    /**
     * 用户向管理员进行举报
     * @param messageReportRequest 举报请求类
     * @param current 当前用户信息
     * @throws MessageException 信息添加失败，信息内容为空，sendId无效或为空
     */
    void report(
            MessageReportRequest messageReportRequest,
            User current) throws MessageException;

    /**
     * 根据当前用户查看信息
     * @param current 当前用户信息
     * @param isRead 是否已读
     * @return 信息列表
     */
    List<Message> getReadOrUnRead(
            User current,
            boolean isRead);

    /**
     * 发送者或收信者单方面删除信息
     * @param current 当前用户信息
     * @param message 信息类
     * @throws MessageException 删除失败
     */
    void delete(
            User current,
            Message message) throws MessageException;

    /**
     * 写信息
     * @param writeRequest 写信息请求类
     * @param current 当前用户信息
     */
    void write(
            MessageWriteRequest writeRequest,
            User current) throws MessageException;

    /**
     * 获取所有信息
     * @param current 当前用户信息
     * @return 信息列表
     */
    List<Message> getMessages(User current);
}
