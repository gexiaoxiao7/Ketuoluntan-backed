package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.able.Likable;
import com.suibe.suibe_mma.domain.request.ReplyIdRequest;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.enumeration.ReplyExceptionEnumeration;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.ReplyMapper;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import com.suibe.suibe_mma.util.ServiceUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 回复服务类实现类
 */
@Service
@Transactional(
        rollbackFor = {ReplyException.class},
        noRollbackFor = {UserException.class, TopicException.class}
        )
public class ReplyServiceImpl
        extends ServiceImpl<ReplyMapper, Reply>
        implements ReplyService {
    /**
     * 注入userService
     */
    @Resource
    private UserService userService;

    /**
     * 注入topicService
     */
    @Resource
    private TopicService topicService;

    /**
     * 注入template
     */
    @Resource
    private RedisTemplate<String, Object> template;

    @Override
    public void writeReply(@NotNull ReplyWriteRequest replyWriteRequest) throws ReplyException {
        Integer userId = replyWriteRequest.getUserId();
        Long topicId = replyWriteRequest.getTopicId();
        String replyContent = replyWriteRequest.getReplyContent();
        try {
            checkId(User.class, userId, userService);
            checkId(Topic.class, topicId, topicService);
            if (replyContent == null || "".equals(replyContent)) {
                ReplyExceptionEnumeration.REPLY_CONTENT_IS_EMPTY.throwReplyException();
            }
            Reply reply = new Reply();
            reply.setUserId(userId);
            reply.setTopicId(topicId);
            reply.setReplyContent(replyContent);
            if (!save(reply)) {
                ReplyExceptionEnumeration.REPLY_SAVE_FAILED.throwReplyException();
            }
            UpdateWrapper<Topic> wrapper = new UpdateWrapper<>();
            wrapper
                    .eq("topicId", topicId)
                    .setSql("replyNum = replyNum + 1")
                    .setSql("updateTime = now()");
            if (!topicService.update(wrapper)) {
                ReplyExceptionEnumeration.REPLY_TOPIC_REPLYNUM_ADD_FAILED.throwReplyException();
            }
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reply> getAllReplyByUserId(Integer id) throws ReplyException {
        try {
            checkId(User.class, id, userService);
            QueryWrapper<Reply> wrapper = new QueryWrapper<>();
            wrapper.eq("userId", id);
            return list(wrapper);
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public Reply like(
            Long replyId,
            Integer userId) throws ReplyException {
        try {
            checkId(User.class, userId, userService);
            Reply reply = checkId(Reply.class, replyId, this);
            String key = "suibe:mma:replyId:" + replyId;
            return ServiceUtil.like(userId, template, key, reply, this, userService);
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reply> getTopicReply(@NotNull Topic topic) throws ReplyException {
        try {
            Long topicId = topic.getTopicId();
            checkId(Topic.class, topicId, topicService);
            QueryWrapper<Reply> wrapper = new QueryWrapper<>();
            wrapper
                    .eq("topicId", topicId)
                    .orderByDesc("replyLikes");
            return list(wrapper);
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public User getAuthor(@NotNull Reply reply) throws ReplyException {
        try {
            Long replyId = reply.getReplyId();
            Reply reply1 = checkId(Reply.class, replyId, this);
            if (reply.equals(reply1)) {
                return checkId(User.class, reply.getUserId(), userService);
            }
            ReplyExceptionEnumeration.REPLY_MESSAGE_WRONG.throwReplyException();
            return null;
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public Integer replyLikeHelp(
            @NotNull Reply reply,
            Integer id) throws ReplyException {
        try {
            checkId(User.class, id, userService);
            return likeOrNot(id, template, "suibe:mma:replyId:" + reply.getReplyId());
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public Long deleteByAuthor(
            @NotNull ReplyIdRequest replyIdRequest,
            Integer userId) throws ReplyException {
        try {
            Long replyId = replyIdRequest.getReplyId();
            checkId(User.class, userId, userService);
            Reply reply = checkId(Reply.class, replyId, this);
            if (reply.getUserId().equals(userId)) {
                if (!removeById(reply)) {
                    ReplyExceptionEnumeration.REPLY_REMOVE_FAILED.throwReplyException();
                }
            } else {
                ReplyExceptionEnumeration.REPLY_USERID_MATCH_FALIED.throwReplyException();
            }
            return replyId;
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }
}
