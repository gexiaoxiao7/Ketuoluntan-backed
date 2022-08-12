package com.suibe.suibe_mma.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.exception.ReplyException;

import java.util.List;

/**
 * 回复服务类接口
 */
public interface ReplyService extends IService<Reply> {
    /**
     * 写回复
     * @param replyWriteRequest 写回复信息类
     * @throws ReplyException 回复内容为空或添加失败
     */
    void writeReply(ReplyWriteRequest replyWriteRequest) throws ReplyException;

    /**
     * 根据用户id获取自己写的所有回复
     * @param id 用户唯一标识
     * @return 回复列表
     * @throws ReplyException 用户id无效
     */
    List<Reply> getAllReplyByUserId(Integer id) throws ReplyException;

    /**
     * 取消点赞或添加点赞
     * @param replyId 回复唯一标识
     * @param userId 用户唯一标识
     * @return 回复信息
     * @throws ReplyException 回复id无效或为空，用户id无效或为空等
     */
    Reply like(Long replyId, Integer userId) throws ReplyException;

    /**
     * 根据题目获取回复
     * @param topic 题目信息
     * @return 回复列表
     * @throws ReplyException 题目id不存在或无效
     */
    List<Reply> getTopicReply(Topic topic) throws ReplyException;

    /**
     * 根据回复信息获取作者信息
     * @param reply 回复信息
     * @return 作者信息
     * @throws ReplyException 用户id无效或为空
     */
    User getAuthor(Reply reply) throws ReplyException;
}
