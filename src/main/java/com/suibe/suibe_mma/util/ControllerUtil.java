package com.suibe.suibe_mma.util;

import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.UserIdRequest;
import com.suibe.suibe_mma.enumeration.TopicEE;
import com.suibe.suibe_mma.enumeration.UserEE;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.suibe.suibe_mma.util.DomainUtil.checkUserRole;

/**
 * controller层工具类
 */
public class ControllerUtil {
    /**
     * 私有化构造器
     */
    private ControllerUtil() {}

    /**
     * 获取当前对象并做验证是否为null
     * @param session session域对象
     * @return 用户信息
     * @throws RuntimeException 无用户登录
     */
    @NotNull
    public static User getCurrent(@NotNull HttpSession session) throws RuntimeException {
        Object o = session.getAttribute(UserService.USER_LOGIN_STATE);
        if (o == null) {
            throw new RuntimeException("当前用户未登录");
        }
        User user = (User) o;
        checkUserRole(user, false);
        return user;
    }

    /**
     * 根据题目删除其下的回复信息
     * @param session session域对象
     * @param topic 题目信息
     * @param replyService 回复服务类
     * @throws TopicException 删除回复失败
     */
    public static void removeReplyByTopic(
            HttpSession session,
            Topic topic,
            @NotNull ReplyService replyService) throws TopicException {
        List<Reply> topicReply = replyService.getTopicReply(topic);
        topicReply.forEach(reply -> {
            reply.setUpdateTime(null);
            session.setAttribute("replyId:" + reply.getReplyId(), null);
        });
        if (!replyService.removeBatchByIds(topicReply)) {
            TopicEE.TOPIC_REMOVE_REPLY_FAILED.throwE();
        }
        session.setAttribute("topicId:" + topic.getTopicId(), null);
    }

    /**
     * 给用户解封或封号帮助方法
     * @param userIdRequest 用户id类
     * @param session session域对象
     * @param userService 用户服务类
     * @param isSeal 解封与封号标志
     * @throws RuntimeException 用户id传递失败等
     */
    public static void sealUserOrNot(
            UserIdRequest userIdRequest,
            HttpSession session,
            UserService userService,
            boolean isSeal) throws RuntimeException {
        requestFail(userIdRequest);
        if (isSeal) {
            userService.sealUser(userIdRequest, getCurrent(session));
        } else {
            userService.unsealUser(userIdRequest, getCurrent(session));
        }
    }

    /**
     * 请求失败帮助方法
     * @param serializable 请求参数
     * @throws RuntimeException 请求失败
     */
    public static void requestFail(Serializable serializable) throws RuntimeException {
        if (serializable == null) {
            throw new RuntimeException("请求失败");
        }
    }

    /**
     * 请求失败帮助方法
     * @param collection 请求参数
     * @throws RuntimeException 请求失败
     */
    public static void requestFail(Collection<?> collection) throws RuntimeException {
        if (collection == null) {
            throw new RuntimeException("请求失败");
        }
    }
}
