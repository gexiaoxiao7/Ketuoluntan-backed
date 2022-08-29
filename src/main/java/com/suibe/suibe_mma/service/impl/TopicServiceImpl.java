package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.SearchTitleRequest;
import com.suibe.suibe_mma.domain.request.TopicIdRequest;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.enumeration.TopicEE;
import com.suibe.suibe_mma.enumeration.UserEE;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.mapper.TopicMapper;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import com.suibe.suibe_mma.util.ServiceUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

import static com.suibe.suibe_mma.util.DomainUtil.checkUserInformation;
import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 题目服务类实现类
 */
@Service
@Transactional
public class TopicServiceImpl
        extends ServiceImpl<TopicMapper, Topic>
        implements TopicService {

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
                TopicEE.TOPIC_TITLE_IS_SPACE.throwE();
            }
            User user = userHelp(userId, userService);
            Topic topic = new Topic();
            topic.setUserId(userId);
            topic.setTopicTitle(topicTitle);
            topic.setTopicContent(topicUploadRequest.getTopicContent());
            if (!save(topic)) {
                TopicEE.TOPIC_INSERT_FAILED.throwE();
            }
            return changeScore(user, 10, userService);
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public Topic like(
            Long topicId,
            Integer id) throws TopicException {
        try {
            userHelp(id, userService);
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
            if (!topic.equals(topic1)) {
                TopicEE.TOPIC_MESSAGE_WRONG.throwE();
            }
            return checkId(User.class, topic.getUserId(), userService);
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
    public Topic deleteByAuthorOrNot(
            @NotNull TopicIdRequest topicIdRequest,
            User user,
            boolean isAuthor) throws TopicException {
        try {
            Integer id = user.getId();
            Long topicId = topicIdRequest.getTopicId();
            User getUser = checkId(User.class, id, userService);
            if (!getUser.equals(user)) {
                UserEE.USER_INFORMATION_WRONG.throwE();
            }
            Topic topic = checkId(Topic.class, topicId, this);
            topicDelete(topic, getUser, this, isAuthor);
            changeScore(id, -10 - topic.getTopicLikes(), userService);
            deleteTopicKey(topic, template);
            return topic;
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public List<Topic> deleteBatchByAuthorOrNot(
            List<Long> ids,
            User user,
            boolean isAuthor) throws TopicException {
        try {
            User getUser = checkId(User.class, user.getId(), userService);
            if (!getUser.equals(user)) {
                UserEE.USER_INFORMATION_WRONG.throwE();
            }
            List<Topic> topics = topicDeleteBatch(ids, getUser, this, isAuthor);
            topics.forEach(topic -> {
                changeScore(topic.getUserId(), -10 - topic.getTopicLikes(), userService);
                deleteTopicKey(topic, template);
            });
            return topics;
        } catch (RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public List<Topic> searchTitle(@NotNull SearchTitleRequest searchTitleRequest) throws TopicException {
        String searchTitle = searchTitleRequest.getSearchTitle();
        if (searchTitle == null || "".equals(searchTitle)) {
            return list();
        }
        String[] split = searchTitle.split("");
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        Arrays.stream(split).forEach(msg -> wrapper.like("topicTitle", msg).or());
        List<Topic> list = list(wrapper);
        if (list.isEmpty()) {
            TopicEE.TOPIC_SEARCH_TITLE_WRONG.throwE();
        }
        return list;
    }

    @Override
    public Topic updateTopicInfo(
            @NotNull Topic topic,
            User current) throws TopicException {
        try {
            Long topicId = topic.getTopicId();
            checkId(Topic.class, topicId, this);
            User user = checkId(User.class, topic.getUserId(), userService);
            checkUserInformation(user, current);
            String topicTitle = topic.getTopicTitle();
            if (topicTitle == null || "".equals(topicTitle)) {
                TopicEE.TOPIC_TITLE_IS_SPACE.throwE();
            }
            topic = notSetNull(topic, new String[]{"topicTitle", "topicContent"});
            if (!updateById(topic)) {
                TopicEE.TOPIC_INFO_UPDATE_FAILED.throwE();
            }
            return getById(topicId);
        } catch (IllegalAccessException | RuntimeException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }

    @Override
    public Topic star(Topic topic, User current) throws TopicException {
        try {
            checkUserInformation(current, userService, false, true);
            boolean flag = false;
            if ("".equals(topic.getIsStared())) {
                topic.setIsStared("精选");
                flag = true;
            } else {
                topic.setIsStared("");
            }
            User user = checkId(User.class, topic.getUserId(), userService);
            topic = notSetNull(topic, "isStared");
            if (!updateById(topic)) {
                TopicEE.TOPIC_IS_STARE_UPDATE_FAILED.throwE();
            }
            if (flag) {
                userService.changeScore(user, 5);
            } else {
                userService.changeScore(user, -5);
            }
            return getById(topic.getTopicId());
        } catch (RuntimeException | IllegalAccessException e) {
            throw new TopicException(e.getMessage(), e);
        }
    }
}
