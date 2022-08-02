package com.suibe.suibe_mma.service.impl;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = {TopicException.class})
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
    public User upload(TopicUploadRequest topicUploadRequest) throws TopicException {
        String topicTitle = topicUploadRequest.getTopicTitle();
        Integer userId = topicUploadRequest.getUserId();
        if (topicTitle == null || "".equals(topicTitle)) {
            TopicExceptionEnumeration.TOPIC_TITLE_IS_SPACE.throwTopicException();
        }
        User user = checkUserId(userId);
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
    }

    @Override
    public Topic like(Integer topicId, Integer id) throws TopicException{
        User user = checkUserId(id);
        Topic topic = checkTopicId(topicId);
        String key = "topicId:" + topicId;
        SetOperations<String, Object> stringObjectSetOperations = template.opsForSet();
        Boolean member = stringObjectSetOperations.isMember(key, user.getId());
        if (member == null || !member) {
            stringObjectSetOperations.add(key, user.getId());
            topic.setTopicLikes(topic.getTopicLikes() + 1);
        } else {
            stringObjectSetOperations.remove(key, user.getId());
            topic.setTopicLikes(topic.getTopicLikes() - 1);
        }
        if (!updateById(topic)) {
            TopicExceptionEnumeration.TOPIC_LIKE_UPDATE_FAILED.throwTopicException();
        }
        Topic returnTopic = getById(topicId);
        returnTopic.setUpdateTime(null);

        return returnTopic;
    }

    /**
     * 检查用户id是否有效或为空
     * @param id 用户唯一标识
     * @return 如果有效则返回用户信息
     * @throws TopicException 用户id无效或为空
     */
    private User checkUserId(Integer id) throws TopicException {
        if (id == null) {
            TopicExceptionEnumeration.TOPIC_USER_ID_IS_NULL.throwTopicException();
        }
        User user = userService.getById(id);
        if (user == null) {
            TopicExceptionEnumeration.TOPIC_USER_ID_IS_WRONG.throwTopicException();
        }
        user.setUserPassword(null);
        user.setUpdateTime(null);
        return user;
    }

    /**
     * 检查题目id是否有效或为空
     * @param id 题目唯一标识
     * @return 题目信息
     * @throws TopicException 题目id无效或为空
     */
    private Topic checkTopicId(Integer id) throws TopicException {
        if (id == null) {
            TopicExceptionEnumeration.TOPIC_ID_IS_NULL.throwTopicException();
        }
        Topic topic = getById(id);
        if (topic == null) {
            TopicExceptionEnumeration.TOPIC_ID_IS_WRONG.throwTopicException();
        }
        topic.setUpdateTime(null);
        return topic;
    }
}
