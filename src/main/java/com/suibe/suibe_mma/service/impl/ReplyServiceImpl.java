package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.ReplyIdRequest;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.enumeration.ReplyEE;
import com.suibe.suibe_mma.enumeration.TopicEE;
import com.suibe.suibe_mma.exception.ReplyException;
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
import javax.servlet.http.HttpSession;

import java.util.List;

import static com.suibe.suibe_mma.util.DomainUtil.checkUserInformation;
import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 回复服务类实现类
 */
@Service
@Transactional
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
    public User writeReply(
            @NotNull ReplyWriteRequest replyWriteRequest,
            User currentUser) throws ReplyException {
        Integer userId = replyWriteRequest.getUserId();
        Long topicId = replyWriteRequest.getTopicId();
        String replyContent = replyWriteRequest.getReplyContent();
        try {
            User getUser = userHelp(userId, userService);
            checkUserInformation(getUser, currentUser);
            checkId(Topic.class, topicId, topicService);
            if (replyContent == null || "".equals(replyContent)) {
                ReplyEE.REPLY_CONTENT_IS_EMPTY.throwE();
            }
            Reply reply = new Reply();
            reply.setUserId(userId);
            reply.setTopicId(topicId);
            reply.setReplyContent(replyContent);
            if (!save(reply)) {
                ReplyEE.REPLY_SAVE_FAILED.throwE();
            }
            UpdateWrapper<Topic> wrapper = new UpdateWrapper<>();
            wrapper
                    .eq("topicId", topicId)
                    .setSql("replyNum = replyNum + 1")
                    .setSql("updateTime = now()");
            if (!topicService.update(wrapper)) {
                ReplyEE.REPLY_TOPIC_REPLYNUM_ADD_FAILED.throwE();
            }
            return changeScore(getUser, 5, userService);
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
            userHelp(userId, userService);
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
            if (!reply.equals(reply1)) {
                ReplyEE.REPLY_MESSAGE_WRONG.throwE();
            }
            return checkId(User.class, reply1.getUserId(), userService);
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
    public Long deleteByAuthorOrNot(
            @NotNull ReplyIdRequest replyIdRequest,
            User user,
            boolean isAuthor) throws ReplyException {
        try {
            Long replyId = replyIdRequest.getReplyId();
            User getUser = checkId(User.class, user.getId(), userService);
            checkUserInformation(getUser, user);
            Reply reply = checkId(Reply.class, replyId, this);
            replyDelete(reply, getUser, this, topicService, isAuthor);
            changeScore(reply.getUserId(), -5 - reply.getReplyLikes(), userService);
            deleteReplyKey(reply, template);
            return replyId;
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public List<Long> deleteBatchByAuthorOrNot(
            List<Long> ids,
            User user,
            boolean isAuthor) throws ReplyException {
        try {
            User getUser = checkId(User.class, user.getId(), userService);
            checkUserInformation(getUser, user);
            List<Reply> replyList = replyDeleteBatch(ids, getUser, this, topicService, isAuthor);
            replyList.forEach(reply -> {
                changeScore(reply.getUserId(), -5 - reply.getReplyLikes(), userService);
                deleteReplyKey(reply, template);
            });
            return ids;
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteBatch(
            List<Reply> topicReply,
            HttpSession session) throws ReplyException {
        try {
            topicReply.forEach(reply -> {
                reply.setUpdateTime(null);
                session.setAttribute("replyId:" + reply.getReplyId(), null);
            });
            if (!removeBatchByIds(topicReply)) {
                TopicEE.TOPIC_REMOVE_REPLY_FAILED.throwE();
            }
            topicReply.forEach(reply -> {
                changeScore(reply.getUserId(), -5 - reply.getReplyLikes(), userService);
                deleteReplyKey(reply, template);
            });
        } catch (RuntimeException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }

    @Override
    public Reply updateReplyInfo(@NotNull Reply reply, User current) throws ReplyException {
        try {
            Long replyId = reply.getReplyId();
            checkId(Reply.class, replyId, this);
            User user = checkId(User.class, reply.getUserId(), userService);
            checkUserInformation(user, current);
            String content = reply.getReplyContent();
            if (content == null || "".equals(content)) {
                ReplyEE.REPLY_CONTENT_IS_EMPTY.throwE();
            }
            reply = notSetNull(reply, "replyContent");
            if (!updateById(reply)) {
                ReplyEE.REPLY_INFO_UPDATE_FAILED.throwE();
            }
            return getById(replyId);
        } catch (RuntimeException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Reply star(Reply reply, User current) throws ReplyException {
        try {
            checkUserInformation(current, userService, false, true);
            boolean flag = false;
            if ("".equals(reply.getIsStared())) {
                reply.setIsStared("true");
                flag = true;
            } else {
                reply.setIsStared("");
            }
            User user = checkId(User.class, reply.getUserId(), userService);
            reply = notSetNull(reply, "isStared");
            if (!updateById(reply)) {
                ReplyEE.REPLY_IS_STARE_UPDATE_FAILED.throwE();
            }
            if (flag) {
                userService.changeScore(user, 5);
            } else {
                userService.changeScore(user, -5);
            }
            return getById(reply.getReplyId());
        } catch (RuntimeException | IllegalAccessException e) {
            throw new ReplyException(e.getMessage(), e);
        }
    }
}
