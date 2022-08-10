package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.exception.ReplyException;

/**
 * 回复服务类接口
 */
public interface ReplyService extends IService<Reply> {
    /**
     * 写回复
     * @param replyWriteRequest 写回复信息类
     * @throws ReplyException 回复异常类
     */
    void writeReply(ReplyWriteRequest replyWriteRequest) throws ReplyException;
}
