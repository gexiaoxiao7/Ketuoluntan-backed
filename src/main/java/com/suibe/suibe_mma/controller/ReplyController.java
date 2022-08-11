package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 回复相关操作类
 */
@RestController
@RequestMapping("/reply")
public class ReplyController {

    /**
     * 注入replyService
     */
    @Resource
    private ReplyService replyService;

    private final Lock lock = new ReentrantLock();

    /**
     * 写回复
     * @param replyWriteRequest 回复请求对象
     * @return 提示信息
     */
    @PostMapping("/writeReply")
    public String writeReply(@RequestBody ReplyWriteRequest replyWriteRequest) {
        if (replyWriteRequest == null) {
            return "请求失败";
        }
        try {
            replyService.writeReply(replyWriteRequest);
            return "回复成功";
        } catch (ReplyException e) {
            return e.getMessage();
        }
    }

    @GetMapping("/getMyReply")
    public List<Reply> getMyReply(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object originUser = session.getAttribute(UserService.USER_LOGIN_STATE);
        if (originUser == null) {
            session.setAttribute("errMsg", "当前无用户登录");
            return null;
        }
        User user = (User) originUser;
        try {
            List<Reply> list = replyService.getAllReplyByUserId(user.getId());
            session.setAttribute("errMsg", null);
            return list;
        } catch (ReplyException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    @PostMapping("/like")
    public Reply like(@RequestBody Reply reply, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (reply == null) {
            session.setAttribute("errMsg", "回复信息传递失败");
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
            Reply reply_plus = replyService.like(reply.getReplyId(), originUser.getId());
            session.setAttribute("errMsg", null);
            return reply_plus;
        } catch (ReplyException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

}
