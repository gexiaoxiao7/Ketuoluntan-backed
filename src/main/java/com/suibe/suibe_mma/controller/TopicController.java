package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.TopicUploadRequest;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
}
