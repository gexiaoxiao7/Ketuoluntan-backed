package com.suibe.suibe_mma.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private Lock lock = new ReentrantLock();

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
    public Topic like(@RequestBody Integer topicId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (topicId == null) {
            return null;
        }
        Object o = session.getAttribute(UserService.USER_LOGIN_STATE);
        if (o == null) {
            return null;
        }
        User originUser = (User) o;
        try {
            lock.lock();
            Topic topic = topicService.like(topicId, originUser.getId());
            session.setAttribute("errMsg", null);
            return topic;
        } catch (TopicException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取某一页中的题目
     * @param current 当前页，1开始
     * @return 题目列表
     */
    @PostMapping("/getTopic/{current}")
    public List<Topic> getTopic(@PathVariable("current") long current) {
        IPage<Topic> page = new Page<>();
        page.setCurrent(current);
        page.setSize(10L);
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("topicLikes");
        return topicService.page(page, wrapper).getRecords();
    }

    /**
     * 获取题目总页数
     * @param request 请求域对象
     * @return 题目总页数
     */
    @PostMapping("/getTopicTotalPages")
    public Long getTopicTotalPages(HttpServletRequest request) {
        IPage<Topic> page = new Page<>();
        page.setSize(10L);
        page.setCurrent(1L);
        IPage<Topic> topicIPage = topicService.page(page);
        long pages = topicIPage.getPages();
        request.getSession().setAttribute(TopicService.TOPIC_TOTAL_PAGES, pages);
        return pages;
    }
}
