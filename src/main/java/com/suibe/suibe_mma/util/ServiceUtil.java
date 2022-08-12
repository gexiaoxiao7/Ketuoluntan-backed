package com.suibe.suibe_mma.util;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.enumeration.ReplyExceptionEnumeration;
import com.suibe.suibe_mma.enumeration.TopicExceptionEnumeration;
import com.suibe.suibe_mma.enumeration.UserExceptionEnumeration;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static com.suibe.suibe_mma.service.UserService.SALT;

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
            return false;
        }
        if (userAccount.length() != 8) {
            return false;
        }
        return !Pattern.compile(".*[^0-9].*").matcher(userAccount).find();
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
            return false;
        }
        if (userPassword.length() < 8) {
            return false;
        }
        return userPassword.matches("[a-zA-Z].*");
    }

    /**
     * 检验用户是否存在
     * @param user 用户信息
     * @param userMapper 用户mapper类
     * @return 是否存在
     */
    public static boolean isUserExist(@NotNull User user, @NotNull UserMapper userMapper) {
        return userMapper.selectByUserAccount(user.getUserAccount()) > 0;
    }

    /**
     * 检验用户是否存在
     * @param userAccount 账户名
     * @param userMapper 用户mapper类
     * @return 是否存在
     */
    public static boolean isUserExist(String userAccount, @NotNull UserMapper userMapper) {
        return userMapper.selectByUserAccount(userAccount) > 0;
    }

    /**
     * 检查题目id是否有效或为空
     * @param id 题目唯一标识
     * @param topicService 题目服务类
     * @return 题目信息
     * @throws TopicException 题目id无效或为空
     */
    public static Topic checkTopicId(Long id, TopicService topicService) throws TopicException {
        if (id == null) {
            TopicExceptionEnumeration.TOPIC_ID_IS_NULL.throwTopicException();
        }
        Topic topic = topicService.getById(id);
        if (topic == null) {
            TopicExceptionEnumeration.TOPIC_ID_IS_WRONG.throwTopicException();
        }
        return topic;
    }

    /**
     * 检查用户id是否有效或为空
     * @param id 用户唯一标识
     * @param userService 用户服务类
     * @return 如果有效则返回用户信息
     * @throws UserException 用户id无效或为空
     */
    public static User checkUserId(Integer id, UserService userService) throws UserException {
        if (id == null) {
            UserExceptionEnumeration.USER_ID_IS_NULL.throwUserException();
        }
        User user = userService.getById(id);
        if (user == null) {
            UserExceptionEnumeration.USER_ID_WRONG.throwUserException();
        }
        return user;
    }

    /**
     * 检查回复id是否有效或为空
     * @param id 回复唯一标识
     * @return 回复信息
     * @throws ReplyException 回复id无效或为空
     */
    public static Reply checkReplyId(Long id, ReplyService replyService) throws ReplyException {
        if (id == null) {
            ReplyExceptionEnumeration.REPLY_ID_IS_NULL.throwTopicException();
        }
        Reply reply = replyService.getById(id);
        if (reply == null) {
            ReplyExceptionEnumeration.REPLY_ID_IS_WRONG.throwTopicException();
        }
        return reply;
    }

    /**
     * 根据flag与userId改变score值
     * @param flag 改变score的标志
     * @param userId 用户唯一标识
     * @return wrapper
     */
    public static UpdateWrapper<User> likeHelper(boolean flag, Integer userId) {
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

}
