package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

import static com.suibe.suibe_mma.util.ServiceUtil.checkTopicId;
import static com.suibe.suibe_mma.util.ServiceUtil.checkUserId;

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
    public User upload(TopicUploadRequest topicUploadRequest) throws TopicException {
        String topicTitle = topicUploadRequest.getTopicTitle();
        Integer userId = topicUploadRequest.getUserId();
        if (topicTitle == null || "".equals(topicTitle)) {
            TopicExceptionEnumeration.TOPIC_TITLE_IS_SPACE.throwTopicException();
        }
        User user;
        try {
            user = checkUserId(userId, userService);
        } catch (UserException e) {
            throw new TopicException(e.getMessage(), e);
        }
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
        User user;
        try {
            user = checkUserId(id, userService);
        } catch (UserException e) {
            throw new TopicException(e.getMessage(), e);
        }
        Topic topic = checkTopicId(topicId, this);
        String key = "suibe:mma:topicId:" + topicId;
        SetOperations<String, Object> operations = template.opsForSet();
        Boolean member = operations.isMember(key, user.getId());
        boolean flag = false;
        if (member == null || !member) {
            operations.add(key, user.getId());
            topic.setTopicLikes(topic.getTopicLikes() + 1);
            flag = true;
        } else {
            operations.remove(key, user.getId());
            topic.setTopicLikes(topic.getTopicLikes() - 1);
        }
        if (!updateById(topic)) {
            TopicExceptionEnumeration.TOPIC_LIKE_UPDATE_FAILED.throwTopicException();
        }
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", topic.getUserId());
        if (flag) {
            wrapper.setSql("score = score + 1");
        } else {
            wrapper.setSql("score = score - 1");
        }
        wrapper.setSql("updateTime = now()");
        userService.update(wrapper);

        return getById(topicId);
    }
}
