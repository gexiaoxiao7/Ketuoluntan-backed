package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.enumeration.TopicExceptionEnumeration;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.TopicMapper;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 题目服务类实现类
 */
@Service
@Transactional(rollbackFor = {TopicException.class}, noRollbackFor = {UserException.class})
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

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
            User user = checkUserId(userId, userService);
            Topic topic = new Topic();
            topic.setUserId(userId);
            topic.setTopicTitle(topicTitle);
            topic.setTopicContent(topicUploadRequest.getTopicContent());
            int count = topicMapper.insert(topic);
            if (count == 0) {
                TopicExceptionEnumeration.TOPIC_INSERT_FAILED.throwTopicException();
            }
            try {
                return userService.changeScore(user, 10);
            } catch (UserException e) {
                throw new TopicException(e.getMessage(), e);
            }
        } catch (UserException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public Topic like(Long topicId, Integer id) throws TopicException {
        try {
            checkUserId(id, userService);
            Topic topic = checkTopicId(topicId, this);
            String key = "suibe:mma:topicId:" + topicId;
            SetOperations<String, Object> operations = template.opsForSet();
            Integer integer = likeOrNot(id, template, key);
            boolean flag = false;
            if (integer == -1) {
                operations.add(key, id);
                topic.setTopicLikes(topic.getTopicLikes() + 1);
                flag = true;
            } else {
                operations.remove(key, id);
                topic.setTopicLikes(topic.getTopicLikes() - 1);
            }
            topic.setUpdateTime(null);
            if (!updateById(topic)) {
                TopicExceptionEnumeration.TOPIC_LIKE_UPDATE_FAILED.throwTopicException();
            }
            userService.update(likeHelper(flag, topic.getUserId()));

            return getById(topicId);
        } catch (UserException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public List<Topic> getAllTopicByUserId(Integer userId) throws TopicException {
        try {
            checkUserId(userId, userService);
            QueryWrapper<Topic> wrapper = new QueryWrapper<>();
            wrapper.eq("userId", userId);
            return list(wrapper);
        } catch (UserException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public User getAuthor(@NotNull Topic topic) throws TopicException {
        try {
            Long topicId = topic.getTopicId();
            Topic topic1 = checkTopicId(topicId, this);
            if (topic.equals(topic1)) {
                return checkUserId(topic.getUserId(), userService);
            }
            TopicExceptionEnumeration.TOPIC_MESSAGE_WRONG.throwTopicException();
            return null;
        } catch (UserException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public Integer topicLikeHelp(@NotNull Topic topic, Integer id) throws TopicException {
        try {
            checkUserId(id, userService);
            return likeOrNot(id, template, "suibe:mma:topicId:" + topic.getTopicId());
        } catch (UserException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }
}
