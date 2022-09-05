package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.Message;
import com.suibe.suibe_mma.domain.StringResponse;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.MessageIdRequest;
import com.suibe.suibe_mma.domain.request.MessageReportRequest;
import com.suibe.suibe_mma.domain.request.MessageWriteRequest;
import com.suibe.suibe_mma.service.MessageService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.suibe.suibe_mma.util.ControllerUtil.getCurrent;
import static com.suibe.suibe_mma.util.ControllerUtil.requestFail;

/**
 * 信息类控制类
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    /**
     * 注入messageService
     */
    @Resource
    private MessageService messageService;

    /**
     * 注入userService
     */
    @Resource
    private UserService userService;

    /**
     * 获取所有信息
     * @param request 请求域对象
     * @return 信息列表
     */
    @PostMapping("/getMessages")
    public List<Message> getMessages(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Message> messages = messageService.getMessages(current);
            session.setAttribute("errMsg", null);
            return messages;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 获取接收的所有信息
     * @param request 请求域对象
     * @return 信息列表
     */
    @PostMapping("/getReceiveMessages")
    public List<Message> getReceiveMessages(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Message> messages = messageService.getReceiveMessages(current);
            session.setAttribute("errMsg", null);
            return messages;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 获取发送的所有信息
     * @param request 请求域对象
     * @return 信息列表
     */
    @PostMapping("/getSendMessages")
    public List<Message> getSendMessages(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Message> messages = messageService.getSendMessages(current);
            session.setAttribute("errMsg", null);
            return messages;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 查看信息内容
     * @param request 请求域对象
     * @param messageIdRequest 信息唯一标识请求类
     * @return 信息具体内容
     */
    @PostMapping("/read")
    public Message read(
            @NotNull HttpServletRequest request,
            @RequestBody MessageIdRequest messageIdRequest) {
        HttpSession session = request.getSession();
        try {
            requestFail(messageIdRequest);
            User current = getCurrent(session, userService);
            Message message = messageService.read(messageIdRequest, current);
            session.setAttribute("errMsg", null);
            return message;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 用户向管理员举报
     * @param request 请求域对象
     * @param messageReportRequest 举报信息请求类
     * @return 提示信息
     */
    @PostMapping("/report")
    public StringResponse report(
            @NotNull HttpServletRequest request,
            @RequestBody MessageReportRequest messageReportRequest) {
        HttpSession session = request.getSession();
        StringResponse stringResponse = new StringResponse();
        try {
            requestFail(messageReportRequest);
            User current = getCurrent(session, userService);
            messageService.report(messageReportRequest, current);
            stringResponse.setMessage("举报成功");
            return stringResponse;
        } catch (RuntimeException e) {
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
        }
    }

    /**
     * 用户给其他用户发信息
     * @param request 请求域对象
     * @param writeRequest 写信息请求类
     * @return 提示信息
     */
    @PostMapping("/write")
    public StringResponse write(
            @NotNull HttpServletRequest request,
            @RequestBody MessageWriteRequest writeRequest) {
        HttpSession session = request.getSession();
        StringResponse stringResponse = new StringResponse();
        try {
            requestFail(writeRequest);
            User current = getCurrent(session, userService);
            messageService.write(writeRequest, current);
            stringResponse.setMessage("发送成功");
            return stringResponse;
        } catch (RuntimeException e) {
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
        }
    }

    /**
     * 获取收者信未读信息列表
     * @param request 请求域对喜感
     * @return 信息列表
     */
    @PostMapping("/getUnRead")
    public List<Message> getUnRead(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Message> unRead = messageService.getReadOrUnRead(current, false);
            session.setAttribute("errMsg", null);
            return unRead;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 获取收者已读信息列表
     * @param request 请求域对象
     * @return 信息列表
     */
    @PostMapping("/getRead")
    public List<Message> getRead(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Message> read = messageService.getReadOrUnRead(current, true);
            session.setAttribute("errMsg", null);
            return read;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 删除信息
     * @param request 请求域对象
     * @param message 信息类
     * @return 提示信息
     */
    @PostMapping("/delete")
    public StringResponse delete(
            @NotNull HttpServletRequest request,
            @RequestBody Message message) {
        HttpSession session = request.getSession();
        StringResponse stringResponse = new StringResponse();
        try {
            requestFail(message);
            User current = getCurrent(session, userService);
            messageService.delete(current, message);
            stringResponse.setMessage("删除成功");
            return stringResponse;
        } catch (RuntimeException e) {
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
        }
    }
}
