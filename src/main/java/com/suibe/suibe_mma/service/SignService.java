package com.suibe.suibe_mma.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Message;
import com.suibe.suibe_mma.domain.Sign;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.MessageIdRequest;
import com.suibe.suibe_mma.domain.request.MessageReportRequest;
import com.suibe.suibe_mma.domain.request.SignIdRequest;
import com.suibe.suibe_mma.domain.request.SignReportRequest;
import com.suibe.suibe_mma.exception.MessageException;
import com.suibe.suibe_mma.exception.SignException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 报名服务类
 */
public interface SignService
        extends IService<Sign> {

    /**
     * 获取当前用户接收的所有报名表
     * @param current 当前用户信息
     * @return 信息列表
     */
    List<Sign> getReceiveSigns(User current);

    /**
     * 用户向管理员进行举报
     * @param signReportRequest 报名请求类
     * @param current 当前用户信息
     * @throws SignException 信息添加失败，信息内容为空，sendId无效或为空
     */
    void signReport(
            SignReportRequest signReportRequest,
            User current) throws SignException;

    /**
     * 发送者或收信者单方面删除信息
     * @param current 当前用户信息
     * @param sign 信息类
     * @throws SignException 删除失败
     */
    void delete(
            User current,
            Sign sign) throws SignException;


    /**
     * 根据当前用户查看信息
     * @param current 当前用户信息
     * @param isRead 是否已读
     * @return 信息列表
     */
    List<Sign> getReadOrUnRead(
            User current,
            boolean isRead);

    Sign read(
            SignIdRequest signIdRequest,
            User current) throws SignException;
}
