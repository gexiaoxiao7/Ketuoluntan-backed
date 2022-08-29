package com.suibe.suibe_mma.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suibe.suibe_mma.SuibeMmaApplication;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.SearchTitleRequest;
import com.suibe.suibe_mma.domain.request.TopicIdRequest;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.suibe.suibe_mma.util.ControllerUtil.*;
import static com.suibe.suibe_mma.util.ServiceUtil.checkId;

/**
 * 题目相关操作控制类
 */
@RestController
@RequestMapping("/topic")
public class TopicController {

    /**
     * 注入topicService
     */
    @Resource
    private TopicService topicService;

    /**
     * 注入replyService
     */
    @Resource
    private ReplyService replyService;

    /**
     * 注入userService
     */
    @Resource
    private UserService userService;

    /**
     * 上传题目控制方法
     * @param topicUploadRequest 题目上传实例
     * @param request 请求域对象
     * @return 上传是否成功提示信息
     */
    @PostMapping("/upload")
    public String upload(
            @RequestBody TopicUploadRequest topicUploadRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(topicUploadRequest);
                getCurrent(session, userService);
                session.setAttribute(
                        UserService.USER_LOGIN_STATE,
                        topicService.upload(topicUploadRequest)
                );
                return "上传成功";
            } catch (RuntimeException e) {
                return e.getMessage();
            }
        }
    }

    /**
     * 取消点赞或者添加点赞
     * @param topic 题目信息
     * @param request 请求域对象
     * @return 题目信息
     */
    @PostMapping("/like")
    public Topic like(
            @RequestBody Topic topic,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(topic);
                Integer id = getCurrent(session, userService).getId();
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
            }
        }
    }

    /**
     * 获取所有题目信息
     * @param request 请求域对象
     * @return 题目列表
     */
    @GetMapping("/getTotalTopic")
    public List<Topic> getTotalTopic(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            QueryWrapper<Topic> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("createTime");
            List<Topic> list = topicService.list(wrapper);
            Integer id = getCurrent(session, userService).getId();
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
     * @param topicIdRequest 题目id类
     * @param request 请求域对象
     * @return 题目信息
     */
    @PostMapping("/getTopicById")
    public Topic getTopicById(
            @RequestBody TopicIdRequest topicIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(topicIdRequest);
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
            List<Topic> list = topicService.getAllTopicByUserId(getCurrent(session, userService).getId());
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
            requestFail(topic);
            User author = topicService.getAuthor(topic);
            session.setAttribute("errMsg", null);
            return author;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 题目作者删除题目
     * @param topicIdRequest 题目id类
     * @param request 请求域对象
     * @return 题目id
     */
    @PostMapping("/deleteByAuthor")
    public Long deleteByAuthor(
            @RequestBody TopicIdRequest topicIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(topicIdRequest);
                Topic topic = topicService.deleteByAuthorOrNot(topicIdRequest, getCurrent(session, userService), true);
                removeReplyByTopic(session, topic, replyService);
                session.setAttribute("errMsg", null);
                return topic.getTopicId();
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 题目作者批量删除题目
     * @param ids 题目id列表
     * @param request 请求域对象
     * @return 题目id列表
     */
    @PostMapping("/deleteBatchByAuthor")
    public List<Long> deleteBatchByAuthor(
            @RequestBody List<Long> ids,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(ids);
                List<Topic> topics = topicService.deleteBatchByAuthorOrNot(ids, getCurrent(session, userService), true);
                topics.forEach(topic -> removeReplyByTopic(session, topic, replyService));
                session.setAttribute("errMsg", null);
                return ids;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 管理员删除题目
     * @param topicIdRequest 题目id类
     * @param request 请求域对象
     * @return 题目id
     */
    @PostMapping("/deleteByManager")
    public Long deleteByManager(
            @RequestBody TopicIdRequest topicIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(topicIdRequest);
                Topic topic = topicService.deleteByAuthorOrNot(topicIdRequest, getCurrent(session, userService), false);
                removeReplyByTopic(session, topic, replyService);
                session.setAttribute("errMsg", null);
                return topic.getTopicId();
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 管理员批量删除题目
     * @param ids 题目id列表
     * @param request 请求域对象
     * @return 题目id列表
     */
    @PostMapping("/deleteBatchByManager")
    public List<Long> deleteBatchByManager(
            @RequestBody List<Long> ids,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(ids);
                List<Topic> topics = topicService.deleteBatchByAuthorOrNot(ids, getCurrent(session, userService), false);
                topics.forEach(topic -> removeReplyByTopic(session, topic, replyService));
                session.setAttribute("errMsg", null);
                return ids;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 根据搜索题目查询题目信息
     * @param searchTitleRequest 查询题目相关请求类
     * @param request 请求域对象
     * @return 题目信息列表
     */
    @PostMapping("/searchTitle")
    public List<Topic> searchTitle(
            @RequestBody SearchTitleRequest searchTitleRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(searchTitleRequest);
            getCurrent(session, userService);
            List<Topic> topics = topicService.searchTitle(searchTitleRequest);
            session.setAttribute("errMsg", null);
            return topics;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 将题目设为精选或取消
     * @param topic 题目信息
     * @param request 请求域对象
     * @return 题目信息
     */
    @PostMapping("/star")
    public Topic star(
            @RequestBody Topic topic,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(topic);
                User current = getCurrent(session, userService);
                Topic topic_plus = topicService.star(topic, current);
                session.setAttribute("errMsg", null);
                return topic_plus;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 题目信息更新
     * @param topic 题目信息
     * @param request 请求域对象
     * @return 题目信息
     */
    @PostMapping("/update")
    public Topic update(Topic topic, @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(topic);
            User current = getCurrent(session, userService);
            Topic topic_plus = topicService.updateTopicInfo(topic, current);
            session.setAttribute("errMsg", null);
            return topic_plus;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }
}
