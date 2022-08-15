package com.suibe.suibe_mma.util;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.able.Checkable;
import com.suibe.suibe_mma.domain.able.Likable;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
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

}
