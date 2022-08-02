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

    @Override
    public User upload(TopicUploadRequest topicUploadRequest) throws TopicException {
        String topicTitle = topicUploadRequest.getTopicTitle();
        Integer userId = topicUploadRequest.getUserId();
        if (topicTitle == null || "".equals(topicTitle)) {
            TopicExceptionEnumeration.TOPIC_TITLE_IS_SPACE.throwTopicException();
        }
        if (userId == null) {
            TopicExceptionEnumeration.TOPIC_USR_ID_IS_NULL.throwTopicException();
        }
        User user = userService.getById(userId);
        if (user == null) {
            TopicExceptionEnumeration.TOPIC_USER_ID_IS_WRONG.throwTopicException();
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
}
