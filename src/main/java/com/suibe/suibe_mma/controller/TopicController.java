package com.suibe.suibe_mma.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicIdRequest;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.enumeration.TopicExceptionEnumeration;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.suibe.suibe_mma.util.ControllerUtil.getCurrent;
import static com.suibe.suibe_mma.util.ServiceUtil.checkId;

/**
 * 题目相关操作控制类
 */
@RestController
@RequestMapping("/topic")
public class TopicController {

    /**
     * 注入TopicService
     */
    @Resource
    private TopicService topicService;

    @Resource
    private ReplyService replyService;

    /**
     * 定义一个锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 上传题目控制方法
     * @param topicUploadRequest 题目上传实例
     * @return 上传是否成功提示信息
     */
    @PostMapping("/upload")
    public String upload(
            @RequestBody TopicUploadRequest topicUploadRequest,
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (topicUploadRequest == null) {
                return "请求失败";
            }
            getCurrent(session);
            session.setAttribute(
                    UserService.USER_LOGIN_STATE,
                    topicService.upload(topicUploadRequest)
            );
            return "上传成功";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    /**
     * 取消点赞或者添加点赞
     * @param request 请求域对象
     * @return 题目信息
     */
    @PostMapping("/like")
    public Topic like(
            @RequestBody Topic topic,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (topic == null) {
                throw new RuntimeException("题目信息传递失败");
            }
            User originUser = getCurrent(session);
            Integer id = originUser.getId();
            lock.lock();
            Topic topic_plus = topicService.like(topic.getTopicId(), id);
            session.setAttribute("errMsg", null);
            if (topicService.topicLikeHelp(topic_plus, id) == 1) {
                session.setAttribute("topicId:" + topic_plus.getTopicId(), 1);
            } else {
                session.setAttribute("topicId:" + topic_plus.getTopicId(), null);
            }
            return topic_plus;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取所有题目信息
     */
    @GetMapping("/getTotalTopic")
    public List<Topic> getTotalTopic(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            QueryWrapper<Topic> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("createTime");
            List<Topic> list = topicService.list(wrapper);
            Integer id = getCurrent(session).getId();
            list.forEach(
                    topic -> {
                        Integer integer = topicService.topicLikeHelp(topic, id);
                        if (integer == 1) {
                            session.setAttribute("topicId:" + topic.getTopicId(), 1);
                        }
                    }
            );
            session.setAttribute("errMsg", null);
            return list;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 根据题目Id获取题目信息
     */
    @PostMapping("/getTopicById")
    public Topic getTopicById(
            @RequestBody TopicIdRequest topicIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (topicIdRequest == null) {
                throw new RuntimeException("题目id传递失败");
            }
            Topic topic = checkId(Topic.class, topicIdRequest.getTopicId(), topicService);
            session.setAttribute("errMsg", null);
            return topic;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 获取登录用户发布的题目
     * @param request 请求域对象
     * @return 题目
     */
    @GetMapping("/getMyTopic")
    public List<Topic> getMyTopic(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            List<Topic> list = topicService.getAllTopicByUserId(getCurrent(session).getId());
            session.setAttribute("errMsg", null);
            return list;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 根据题目信息获取作者信息
     * @param topic 题目信息
     * @param request 请求域对象
     * @return 作者信息
     */
    @PostMapping("/getAuthor")
    public User getAuthor(
            @RequestBody Topic topic,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (topic == null) {
                throw new RuntimeException("题目信息传递失败");
            }
            User author = topicService.getAuthor(topic);
            session.setAttribute("errMsg", null);
            return author;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    @PostMapping("/deleteByAuthor")
    public Long deleteByAuthor(
            @RequestBody TopicIdRequest topicIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (topicIdRequest == null) {
                throw new RuntimeException("题目id传递失败");
            }
            Topic topic = topicService.deleteByAuthor(topicIdRequest, getCurrent(session).getId());
            List<Reply> topicReply = replyService.getTopicReply(topic);
            topicReply.forEach(reply -> {
                reply.setUpdateTime(null);
                session.setAttribute("replyId:" + reply.getReplyId(), null);
            });
            if (!replyService.removeBatchByIds(topicReply)) {
                TopicExceptionEnumeration.TOPIC_REMOVE_REPLY_FAILED.throwTopicException();
            }
            Long topicId = topic.getTopicId();
            session.setAttribute("topicId:" + topicId, null);
            session.setAttribute("errMsg", null);
            return topicId;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }
}
