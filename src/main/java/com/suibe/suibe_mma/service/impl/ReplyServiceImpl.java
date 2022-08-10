package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.mapper.ReplyMapper;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 回复服务类实现类
 */
@Service
@Transactional
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements ReplyService {
    @Resource
    private UserService userService;

    @Resource
    private TopicService topicService;

    @Override
    public void writeReply(ReplyWriteRequest replyWriteRequest) throws ReplyException {
        Integer userId = replyWriteRequest.getUserId();
        Long topicId = replyWriteRequest.getTopicId();
        String replyContent = replyWriteRequest.getReplyContent();

    }
}
