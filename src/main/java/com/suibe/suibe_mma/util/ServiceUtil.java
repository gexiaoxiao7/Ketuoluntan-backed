package com.suibe.suibe_mma.util;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.able.Checkable;
import com.suibe.suibe_mma.domain.able.Likable;
import com.suibe.suibe_mma.enumeration.ReplyEE;
import com.suibe.suibe_mma.enumeration.TopicEE;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.suibe.suibe_mma.service.UserService.SALT;
import static com.suibe.suibe_mma.util.DomainUtil.*;

/**
 * Service层工具类
 */
public class ServiceUtil {

    /**
     * 私有化构造方法
     */
    private ServiceUtil() {}

    /**
     * 使用md5加密
     * @param message 加密信息
     * @return 加密后的信息
     */
    @NotNull
    public static String encrypt(String message) {
        return DigestUtils.md5DigestAsHex((SALT + message + SALT).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 检验账户名格式
     * 不为null和""
     * 长度为8
     * 不能有数字之外的值
     * @param userAccount 账户名信息
     * @return 是否符合
     */
    public static boolean checkUserAccount(String userAccount) {
        if (userAccount == null || "".equals(userAccount)) {
            return true;
        }
        if (userAccount.length() != 8) {
            return true;
        }
        return Pattern.compile(".*[^0-9].*").matcher(userAccount).find();
    }

    /**
     * 检验密码格式
     * 不为null和""
     * 长度大于等于8
     * 以字母开头
     * @param userPassword 密码信息
     * @return 是否符合
     */
    public static boolean checkUserPassword(String userPassword) {
        if (userPassword == null || "".equals(userPassword)) {
            return true;
        }
        if (userPassword.length() < 8) {
            return true;
        }
        return !userPassword.matches("[a-zA-Z].*");
    }

    /**
     * 检验用户是否存在
     * @param user 用户信息
     * @param userMapper 用户mapper类
     * @return 是否存在
     */
    public static boolean isUserExist(
            @NotNull User user,
            @NotNull UserMapper userMapper) {
        return userMapper.selectByUserAccount(user.getUserAccount()) > 0;
    }

    /**
     * 检验用户是否存在
     * @param userAccount 账户名
     * @param userMapper 用户mapper类
     * @return 是否存在
     */
    public static boolean isUserExist(
            String userAccount,
            @NotNull UserMapper userMapper) {
        return userMapper.selectByUserAccount(userAccount) > 0;
    }

    /**
     * 检查id是否有效或为空
     * @param id 唯一标识
     * @param service 相关服务类
     * @return 如果有效则返回相关信息
     * @throws RuntimeException id无效或为空
     */
    public static <T extends Checkable<T, R>, R> T checkId(
            @NotNull Class<T> clazz,
            R id,
            IService<T> service) throws RuntimeException {
        try {
            return clazz.newInstance().checkPrimaryKey(id, service);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 根据flag与userId改变score值
     * @param flag 改变score的标志
     * @param userId 用户唯一标识
     * @return wrapper
     */
    @NotNull
    public static UpdateWrapper<User> likeHelper(
            boolean flag,
            Integer userId) {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", userId);
        if (flag) {
            wrapper.setSql("score = score + 1");
        } else {
            wrapper.setSql("score = score - 1");
        }
        wrapper.setSql("updateTime = now()");
        return wrapper;
    }

    /**
     * 判断当前登录用户是否点赞题目或回复
     * @param userId 用户唯一标识
     * @param template redis模板类
     * @param key 键
     * @return 标志
     */
    public static Integer likeOrNot(
            Integer userId,
            @NotNull RedisTemplate<String, Object> template,
            String key) {
        Boolean member = template.opsForSet().isMember(key, userId);
        if (member == null || !member) {
            return -1;
        }
        return 1;
    }

    /**
     * 点赞接口帮助方法
     * @param userId 用户唯一标识
     * @param template redis模板类
     * @param key 键
     * @param t 点赞类
     * @param service 相关服务类
     * @param userService 用户服务类
     * @return 点赞类信息
     */
    public static <T extends Likable<T>> T like(
            Integer userId,
            RedisTemplate<String, Object> template,
            String key,
            @NotNull T t,
            IService<T> service,
            UserService userService) throws RuntimeException {
        return t.like(userId, likeOrNot(userId, template, key), template, key, service, userService);
    }

    /**
     * 删除回复帮助方法
     * @param reply 回复信息
     * @param user 用户信息
     * @param replyService 回复服务类
     * @param topicService 题目服务类
     * @param isAuthor 是否是作者自己删
     * @throws RuntimeException 删除回复失败，相关题目replyNum更新失败，用户id不匹配，不为管理员
     */
    public static void replyDelete(
            Reply reply,
            @NotNull User user,
            @NotNull ReplyService replyService,
            TopicService topicService,
            boolean isAuthor) throws RuntimeException {
        checkReplyUserId(reply, user.getId(), isAuthor);
        checkUserRole(user, !isAuthor);
        reply.setUpdateTime(null);
        if (!replyService.removeById(reply)) {
            ReplyEE.REPLY_REMOVE_FAILED.throwE();
        }
        UpdateWrapper<Topic> wrapper = new UpdateWrapper<>();
        wrapper
                .eq("topicId", reply.getTopicId())
                .setSql("updateTime = now()")
                .setSql("replyNum = replyNum - 1");
        if (!topicService.update(wrapper)) {
            ReplyEE.REPLY_TOPIC_REPLYNUM_SUB_FAILED.throwE();
        }
    }

    /**
     * 批量删除回复帮助方法
     * @param ids 回复id列表
     * @param user 用户信息
     * @param replyService 回复服务类
     * @param topicService 题目服务类
     * @param isAuthor 是否是作者自己删
     * @throws RuntimeException 删除回复失败，相关题目replyNum更新失败，用户id不匹配，不为管理员
     */
    public static void replyDeleteBatch(
            List<Long> ids,
            User user,
            @NotNull ReplyService replyService,
            TopicService topicService,
            boolean isAuthor) throws RuntimeException {
        checkUserRole(user, !isAuthor);
        List<Reply> replies = replyService.listByIds(ids);
        List<Long> topicIds = new ArrayList<>(replies.size());
        Map<String, Long> map = new HashMap<>();
        Integer userId = user.getId();
        replies.forEach(reply -> {
            checkReplyUserId(reply, userId, isAuthor);
            reply.setUpdateTime(null);
            topicIds.add(reply.getReplyId());
        });
        if (!replyService.removeBatchByIds(replies)) {
            ReplyEE.REPLY_REMOVE_FAILED.throwE();
        }
        topicIds.forEach(topicId -> {
            if (map.containsKey("topicId:" + topicId)) {
                map.put("topicId:" + topicId, map.get("topicId:" + topicId) + 1L);
            } else {
                map.put("topicId:" + topicId, 1L);
            }
        });
        map.forEach((key, value) -> {
            UpdateWrapper<Topic> wrapper = new UpdateWrapper<>();
            wrapper
                    .eq("topicId", Long.parseLong(key.split(":")[1]))
                    .setSql("updateTime = now()")
                    .setSql("replyNum = replyNum - " + value);
            if (!topicService.update(wrapper)) {
                ReplyEE.REPLY_TOPIC_REPLYNUM_SUB_FAILED.throwE();
            }
        });
    }

    /**
     * 题目删除帮助方法
     * @param topic 题目信息
     * @param user 用户信息
     * @param topicService 题目服务类
     * @param isAuthor 是否是作者
     * @throws RuntimeException 题目删除失败，用户id不匹配，不为管理员
     */
    public static void topicDelete(
            Topic topic,
            @NotNull User user,
            @NotNull TopicService topicService,
            boolean isAuthor) throws RuntimeException {
        checkTopicUserId(topic, user.getId(), isAuthor);
        checkUserRole(user, !isAuthor);
        topic.setUpdateTime(null);
        if (!topicService.removeById(topic)) {
            TopicEE.TOPIC_REMOVE_FAILED.throwE();
        }
    }

    /**
     * 题目批量删除帮助方法
     * @param ids 题目id列表
     * @param user 用户信息
     * @param topicService 题目服务类
     * @param isAuthor 是否是作者
     * @return 题目信息列表
     * @throws RuntimeException 题目删除失败，用户id不匹配，不为管理员
     */
    @NotNull
    public static List<Topic> topicDeleteBatch(
            List<Long> ids,
            User user,
            @NotNull TopicService topicService,
            boolean isAuthor) throws RuntimeException {
        checkUserRole(user, !isAuthor);
        List<Topic> topics = topicService.listByIds(ids);
        Integer userId = user.getId();
        topics.forEach(topic -> {
            checkTopicUserId(topic, userId, isAuthor);
            topic.setUpdateTime(null);
        });
        if (!topicService.removeBatchByIds(topics)) {
            TopicEE.TOPIC_REMOVE_FAILED.throwE();
        }
        return topics;
    }

}
