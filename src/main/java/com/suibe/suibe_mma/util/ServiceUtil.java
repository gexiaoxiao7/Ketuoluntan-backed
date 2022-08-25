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
import com.suibe.suibe_mma.enumeration.UserEE;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.exception.UserException;
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

    public static <T extends Checkable<T, R>, R> List<T> checkId(
            @NotNull Class<T> clazz,
            List<R> ids,
            IService<T> service) throws RuntimeException {
        try {
            return clazz.newInstance().checkPrimaryKey(ids, service);
        } catch (IllegalAccessException | InstantiationException e) {
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
        checkUserRole(user, !isAuthor, false);
        reply.setUpdateTime(null);
        if (!replyService.removeById(reply)) {
            ReplyEE.REPLY_REMOVE_FAILED.throwE();
        }
        replyTopicHelp(reply.getTopicId(), 1L, topicService);
    }

    /**
     * 批量删除回复帮助方法
     * @param ids 回复id列表
     * @param user 用户信息
     * @param replyService 回复服务类
     * @param topicService 题目服务类
     * @param isAuthor 是否是作者自己删
     * @return 回复列表
     * @throws RuntimeException 删除回复失败，相关题目replyNum更新失败，用户id不匹配，不为管理员
     */
    @NotNull
    public static List<Reply> replyDeleteBatch(
            @NotNull List<Long> ids,
            User user,
            @NotNull ReplyService replyService,
            TopicService topicService,
            boolean isAuthor) throws RuntimeException {
        checkUserRole(user, !isAuthor, false);
        List<Reply> replies = checkId(Reply.class, ids, replyService);
        List<Long> topicIds = new ArrayList<>(replies.size());
        Map<String, Long> map = new HashMap<>();
        Integer userId = user.getId();
        replies.forEach(reply -> {
            checkReplyUserId(reply, userId, isAuthor);
            reply.setUpdateTime(null);
            topicIds.add(reply.getTopicId());
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
        map.forEach((key, value) -> replyTopicHelp(Long.parseLong(key.split(":")[1]), value, topicService));
        return replies;
    }

    /**
     * 回复删除减少题目replyNum帮助方法
     * @param topicId 题目id
     * @param replyNum 回复数减少数
     * @param topicService 题目服务类
     * @throws ReplyException 题目replyNum更新失败
     */
    public static void replyTopicHelp(
            Long topicId,
            Long replyNum,
            @NotNull TopicService topicService) throws ReplyException {
        UpdateWrapper<Topic> wrapper = new UpdateWrapper<>();
        wrapper
                .eq("topicId", topicId)
                .setSql("updateTime = now()")
                .setSql("replyNum = replyNum - " + replyNum);
        if (!topicService.update(wrapper)) {
            ReplyEE.REPLY_TOPIC_REPLYNUM_SUB_FAILED.throwE();
        }
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
        checkUserRole(user, !isAuthor, false);
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
        checkUserRole(user, !isAuthor, false);
        List<Topic> topics = checkId(Topic.class, ids, topicService);
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

    /**
     * 题目改变用户score帮助方法
     * @param userId 用户唯一标识
     * @param score 改变分数值
     * @param userService 用户服务类
     * @throws UserException 改变用户score失败
     */
    public static void changeScore(
            Integer userId,
            Integer score,
            @NotNull UserService userService) throws UserException {
        userService.changeScore(userService.getById(userId), score);
    }

    /**
     * 题目改变用户score帮助方法
     * @param user 用户信息
     * @param score 改变分数值
     * @param userService 用户服务类
     * @return 用户信息
     * @throws UserException 改变用户score失败
     */
    public static User changeScore(
            User user,
            Integer score,
            @NotNull UserService userService) throws UserException {
        return userService.changeScore(user, score);
    }

    /**
     * 回复点赞信息删除方法
     * @param reply 回复信息
     * @param template redis模板类
     * @throws ReplyException 点赞信息更新失败
     */
    public static void deleteReplyKey(
            @NotNull Reply reply,
            @NotNull RedisTemplate<String, Object> template) throws ReplyException {
        String key = "suibe:mma:replyId:" + reply.getReplyId();
        Boolean hasKey = template.hasKey(key);
        if (hasKey != null && hasKey) {
            Boolean delete = template.delete(key);
            if (delete == null || !delete) {
                ReplyEE.REPLY_LIKE_UPDATE_FAILED.throwE();
            }
        }
    }

    /**
     * 题目点赞信息删除方法
     * @param topic 题目信息
     * @param template redis模板类
     * @throws TopicException 点赞信息更新失败
     */
    public static void deleteTopicKey(
            @NotNull Topic topic,
            @NotNull RedisTemplate<String, Object> template) throws TopicException {
        String key = "suibe:mma:topicId:" + topic.getTopicId();
        Boolean hasKey = template.hasKey(key);
        if (hasKey != null && hasKey) {
            Boolean delete = template.delete(key);
            if (delete == null || !delete) {
                TopicEE.TOPIC_LIKE_UPDATE_FAILED.throwE();
            }
        }
    }

    /**
     * 简化用户检查方法
     * @param id 用户唯一标识
     * @param userService 用户服务类
     * @param isManager 是否是管理员
     * @param isCommon 是否是普通用户
     * @return 用户信息
     * @throws RuntimeException id为空或无效，被封，不是管理员，不是普通用户
     */
    public static User userHelp(Integer id, UserService userService, boolean isManager, boolean isCommon) throws RuntimeException{
        User user = checkId(User.class, id, userService);
        checkUserRole(user, isManager, isCommon);
        return user;
    }

    /**
     * 简化用户检查方法
     * @param id 用户唯一标识
     * @param userService 用户服务类
     * @return 用户信息
     * @throws RuntimeException id为空或无效，被封
     */
    public static User userHelp(Integer id, UserService userService) throws RuntimeException {
        return userHelp(id, userService, false, false);
    }

    /**
     * 简化用户检查方法
     * @param id 用户唯一标识
     * @param userService 用户服务类
     * @param isSeal 是否被封
     * @return 用户信息
     * @throws RuntimeException 用户未被封，被封，id无效或为空
     */
    public static User userHelp(Integer id, UserService userService, boolean isSeal) throws RuntimeException {
        if (!isSeal) {
            return userHelp(id, userService);
        }
        User user = checkId(User.class, id, userService);
        if (user.getUserRole() != 2) {
            UserEE.USER_NOT_SEAL.throwE();
        }
        return user;
    }

}
