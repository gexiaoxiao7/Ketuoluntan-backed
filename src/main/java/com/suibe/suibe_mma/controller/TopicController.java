package com.suibe.suibe_mma.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicIdRequest;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.exception.TopicException;
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

import static com.suibe.suibe_mma.util.ServiceUtil.checkTopicId;

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
    public String upload(@RequestBody TopicUploadRequest topicUploadRequest, HttpServletRequest request) {
        if (topicUploadRequest == null) {
            return "请求失败";
        }
        try {
            request.getSession().setAttribute(UserService.USER_LOGIN_STATE, topicService.upload(topicUploadRequest));
            return "上传成功";
        } catch (TopicException e) {
            return e.getMessage();
        }
    }

    /**
     * 取消点赞或者添加点赞
     * @param request 请求域对象
     * @return 题目信息
     */
    @PostMapping("/like")
    public Topic like(@RequestBody Topic topic, @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (topic == null) {
            session.setAttribute("errMsg", "题目信息传递失败");
            return null;
        }
        Object o = session.getAttribute(UserService.USER_LOGIN_STATE);
        if (o == null) {
            session.setAttribute("errMsg", "当前无用户登录");
            return null;
        }
        User originUser = (User) o;
        try {
            lock.lock();
            Topic topic_plus = topicService.like(topic.getTopicId(), originUser.getId());
            session.setAttribute("errMsg", null);
            return topic_plus;
        } catch (TopicException e) {
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
    public List<Topic> getTotalTopic() {
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("createTime");
        return topicService.list(wrapper);
    }

    /**
     * 根据题目Id获取题目信息
     */
    @PostMapping("/getTopicById")
    public Topic getTopicById(@RequestBody TopicIdRequest topicIdRequest, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (topicIdRequest == null) {
            session.setAttribute("errMsg", "题目id传递失败");
            return null;
        }
        try {
            Topic topic = checkTopicId(topicIdRequest.getTopicId(), topicService);
            session.setAttribute("errMsg", null);
            return topic;
        } catch (TopicException e) {
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
        Object originUser = session.getAttribute(UserService.USER_LOGIN_STATE);
        if (originUser == null) {
            session.setAttribute("errMsg", "当前无用户登录");
            return null;
        }
        User user = (User) originUser;
        try {
            List<Topic> list = topicService.getAllTopicByUserId(user.getId());
            session.setAttribute("errMsg", null);
            return list;
        } catch (TopicException e) {
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
    public User getAuthor(@RequestBody Topic topic, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (topic == null) {
            session.setAttribute("errMsg", "题目信息传递失败");
            return null;
        }
        try {
            User author = topicService.getAuthor(topic);
            session.setAttribute("errMsg", null);
            return author;
        } catch (TopicException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }
}
