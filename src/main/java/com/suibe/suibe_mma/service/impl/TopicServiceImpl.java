package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicIdRequest;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.enumeration.TopicExceptionEnumeration;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.TopicMapper;
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
 * 题目服务类实现类
 */
@Service
@Transactional(
        rollbackFor = {TopicException.class},
        noRollbackFor = {UserException.class}
        )
public class TopicServiceImpl
        extends ServiceImpl<TopicMapper, Topic>
        implements TopicService {

    /**
     * 注入TopicMapper
     */
    @Resource
    private TopicMapper topicMapper;

    /**
     * 注入UserService
     */
    @Resource
    private UserService userService;

    /**
     * 注入RedisTemplate
     */
    @Resource
    private RedisTemplate<String, Object> template;

    @Override
    public User upload(@NotNull TopicUploadRequest topicUploadRequest) throws TopicException {
        try {
            String topicTitle = topicUploadRequest.getTopicTitle();
            Integer userId = topicUploadRequest.getUserId();
            if (topicTitle == null || "".equals(topicTitle)) {
                TopicExceptionEnumeration.TOPIC_TITLE_IS_SPACE.throwTopicException();
            }
            User user = checkId(User.class, userId, userService);
            Topic topic = new Topic();
            topic.setUserId(userId);
            topic.setTopicTitle(topicTitle);
            topic.setTopicContent(topicUploadRequest.getTopicContent());
            int count = topicMapper.insert(topic);
            if (count == 0) {
                TopicExceptionEnumeration.TOPIC_INSERT_FAILED.throwTopicException();
            }
            return userService.changeScore(user, 10);
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public Topic like(
            Long topicId,
            Integer id) throws TopicException {
        try {
            checkId(User.class, id, userService);
            Topic topic = checkId(Topic.class, topicId, this);
            String key = "suibe:mma:topicId:" + topicId;
            return ServiceUtil.like(id, template, key, topic, this, userService);
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public List<Topic> getAllTopicByUserId(Integer userId) throws TopicException {
        try {
            checkId(User.class, userId, userService);
            QueryWrapper<Topic> wrapper = new QueryWrapper<>();
            wrapper.eq("userId", userId);
            return list(wrapper);
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public User getAuthor(@NotNull Topic topic) throws TopicException {
        try {
            Long topicId = topic.getTopicId();
            Topic topic1 = checkId(Topic.class, topicId, this);
            if (topic.equals(topic1)) {
                return checkId(User.class, topic.getUserId(), userService);
            }
            TopicExceptionEnumeration.TOPIC_MESSAGE_WRONG.throwTopicException();
            return null;
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public Integer topicLikeHelp(
            @NotNull Topic topic,
            Integer id) throws TopicException {
        try {
            checkId(User.class, id, userService);
            return likeOrNot(id, template, "suibe:mma:topicId:" + topic.getTopicId());
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public Topic deleteByAuthor(
            @NotNull TopicIdRequest topicIdRequest,
            Integer userId) throws TopicException {
        try {
            Long topicId = topicIdRequest.getTopicId();
            checkId(User.class, userId, userService);
            Topic topic = checkId(Topic.class, topicId, this);
            if (topic.getUserId().equals(userId)) {
                if (!removeById(topic)) {
                    TopicExceptionEnumeration.TOPIC_REMOVE_FAILED.throwTopicException();
                }
            } else {
                TopicExceptionEnumeration.TOPIC_USERID_MATCH_FAILED.throwTopicException();
            }
            return topic;
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }
}
