package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.ReplyIdRequest;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.service.ReplyService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.suibe.suibe_mma.util.ControllerUtil.getCurrent;

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

    /**
     * 锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 写回复
     * @param replyWriteRequest 回复请求对象
     * @return 提示信息
     */
    @PostMapping("/writeReply")
    public String writeReply(@RequestBody ReplyWriteRequest replyWriteRequest) {
        try {
            if (replyWriteRequest == null) {
                return "请求失败";
            }
            replyService.writeReply(replyWriteRequest);
            return "回复成功";
        } catch (ReplyException e) {
            return e.getMessage();
        }
    }

    /**
     * 根据当前登录用户id获取自己写的回复
     * @param request 请求域对象
     * @return 回复列表
     */
    @GetMapping("/getMyReply")
    public List<Reply> getMyReply(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            List<Reply> list = replyService.getAllReplyByUserId(getCurrent(session).getId());
            session.setAttribute("errMsg", null);
            return list;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 对回复进行点赞
     * @param reply 回复信息
     * @param request 请求域对象
     * @return 更新后回复信息
     */
    @PostMapping("/like")
    public Reply like(
            @RequestBody Reply reply,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (reply == null) {
                throw new RuntimeException("回复信息传递失败");
            }
            lock.lock();
            Integer id = getCurrent(session).getId();
            Reply reply_plus = replyService.like(reply.getReplyId(), id);
            session.setAttribute("errMsg", null);
            Integer integer = replyService.replyLikeHelp(reply_plus, id);
            if (integer == 1) {
                session.setAttribute("replyId:" + reply_plus.getReplyId(), 1);
            } else {
                session.setAttribute("replyId:" + reply_plus.getReplyId(), null);
            }
            return reply_plus;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据题目信息获取回复
     * @param topic 题目信息
     * @param request 请求域对象
     * @return 回复列表
     */
    @PostMapping("/getTopicReply")
    public List<Reply> getTopicReply(
            @RequestBody Topic topic,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (topic == null) {
                throw new RuntimeException("题目信息传递失败");
            }
            Integer id = getCurrent(session).getId();
            List<Reply> replies = replyService.getTopicReply(topic);
            replies.forEach(
                    reply -> {
                        Integer integer = replyService.replyLikeHelp(reply, id);
                        if (integer == 1) {
                            session.setAttribute("replyId:" + reply.getReplyId(), 1);
                        }
                    }
            );
            session.setAttribute("errMsg", null);
            return replies;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 根据回复信息获取作者信息
     * @param reply 回复信息
     * @param request 请求域对象
     * @return 作者信息
     */
    @PostMapping("/getAuthor")
    public User getAuthor(
            @RequestBody Reply reply,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (reply == null) {
                throw new RuntimeException("回复信息传递失败");
            }
            User author = replyService.getAuthor(reply);
            session.setAttribute("errMsg", null);
            return author;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 回复作者删除该回复
     * @param replyIdRequest 回复id
     * @param request 请求域对象
     * @return 回复id
     */
    @PostMapping("/deleteByAuthor")
    public Long deleteByAuthor(
            @RequestBody ReplyIdRequest replyIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (replyIdRequest == null) {
                throw new RuntimeException("回复id传递失败");
            }
            Long replyId = replyService.deleteByAuthor(replyIdRequest, getCurrent(session).getId());
            session.setAttribute("replyId:" + replyId, null);
            session.setAttribute("errMsg", null);
            return replyId;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

}
