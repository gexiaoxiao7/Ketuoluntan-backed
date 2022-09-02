package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.SuibeMmaApplication;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.StringResponse;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.ReplyIdRequest;
import com.suibe.suibe_mma.domain.request.ReplyWriteRequest;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.suibe.suibe_mma.util.ControllerUtil.getCurrent;
import static com.suibe.suibe_mma.util.ControllerUtil.requestFail;

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
     * 注入userService
     */
    @Resource
    private UserService userService;

    /**
     * 写回复
     * @param replyWriteRequest 回复请求对象
     * @param request 请求域对象
     * @return 提示信息
     */
    @PostMapping("/writeReply")
    public StringResponse writeReply(
            @RequestBody ReplyWriteRequest replyWriteRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(replyWriteRequest);
                User current = getCurrent(session, userService);
                session.setAttribute(
                        UserService.USER_LOGIN_STATE,
                        replyService.writeReply(replyWriteRequest, current)
                );
                StringResponse stringResponse = new StringResponse();
                stringResponse.setMessage("回复成功");
                return stringResponse;
            } catch (RuntimeException e) {
                StringResponse stringResponse = new StringResponse();
                stringResponse.setMessage(e.getMessage());
                return stringResponse;
            }
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
            Integer id = getCurrent(session, userService).getId();
            List<Reply> list = replyService.getAllReplyByUserId(id);
            list.forEach(
                    reply -> {
                        Integer integer = replyService.replyLikeHelp(reply, id);
                        if (integer == 1) {
                            session.setAttribute("replyId:" + reply.getReplyId(), 1);
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
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(reply);
                Integer id = getCurrent(session, userService).getId();
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
            }
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
            requestFail(topic);
            Integer id = getCurrent(session, userService).getId();
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
            requestFail(reply);
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
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(replyIdRequest);
                Long replyId = replyService.deleteByAuthorOrNot(replyIdRequest, getCurrent(session, userService), true);
                session.setAttribute("replyId:" + replyId, null);
                session.setAttribute("errMsg", null);
                return replyId;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 作者批量删除回复
     * @param ids 回复id列表
     * @param request 请求域对象
     * @return 回复id列表
     */
    @PostMapping("/deleteBatchByAuthor")
    public List<Long> deleteBatchByAuthor(
            @RequestBody List<Long> ids,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(ids);
                replyService
                        .deleteBatchByAuthorOrNot(ids, getCurrent(session, userService), true)
                        .forEach(replyId -> session.setAttribute("replyId:" + replyId, null));
                session.setAttribute("errMsg", null);
                return ids;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 管理员删除回复
     * @param replyIdRequest 回复id类
     * @param request 请求域对象
     * @return 回复id
     */
    @PostMapping("/deleteByManager")
    public Long deleteByManager(
            @RequestBody ReplyIdRequest replyIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(replyIdRequest);
                Long replyId = replyService.deleteByAuthorOrNot(replyIdRequest, getCurrent(session, userService), false);
                session.setAttribute("replyId:" + replyId, null);
                session.setAttribute("errMsg", null);
                return replyId;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 管理员批量删除回复
     * @param ids 回复id列表
     * @param request 请求域对象
     * @return 回复id列表
     */
    @PostMapping("/deleteBatchByManager")
    public List<Long> deleteBatchByManager(
            @RequestBody List<Long> ids,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(ids);
                replyService
                        .deleteBatchByAuthorOrNot(ids, getCurrent(session, userService), false)
                        .forEach(replyId -> session.setAttribute("replyId:" + replyId, null));
                session.setAttribute("errMsg", null);
                return ids;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 更新回复信息
     * @param reply 回复信息
     * @param request 请求域对象
     * @return 回复信息
     */
    @PostMapping("/update")
    public Reply update(
            Reply reply,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(reply);
            User current = getCurrent(session, userService);
            Reply reply_plus = replyService.updateReplyInfo(reply, current);
            session.setAttribute("errMsg", null);
            return reply_plus;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 管理员设置或取消精选
     * @param reply 回复信息
     * @param request 请求域对象
     * @return 回复信息
     */
    @PostMapping("/star")
    public Reply star(
            @RequestBody Reply reply,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(reply);
                User current = getCurrent(session, userService);
                Reply reply_plus = replyService.star(reply, current);
                session.setAttribute("errMsg", null);
                return reply_plus;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

}
