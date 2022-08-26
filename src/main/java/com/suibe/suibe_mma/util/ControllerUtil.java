package com.suibe.suibe_mma.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suibe.suibe_mma.SuibeMmaApplication;
import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.redis.UserIdAndMonthScore;
import com.suibe.suibe_mma.domain.redis.UserIdAndScore;
import com.suibe.suibe_mma.domain.request.ScoreSetRequest;
import com.suibe.suibe_mma.domain.request.UserIdRequest;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.suibe.suibe_mma.util.DomainUtil.checkUserInformation;
import static com.suibe.suibe_mma.util.DomainUtil.checkUserRole;
import static com.suibe.suibe_mma.util.ServiceUtil.encrypt;
import static com.suibe.suibe_mma.util.ServiceUtil.userHelp;

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
     * @param userService 用户服务类
     * @return 用户信息
     * @throws RuntimeException 无用户登录
     */
    @NotNull
    public static User getCurrent(
            @NotNull HttpSession session,
            UserService userService) throws RuntimeException {
        return getCurrent(session, userService, false, false);
    }

    /**
     * 获取当前对象并做验证是否为null
     * @param session session域对象
     * @param userService 用户服务类
     * @param isManager 是否是管理员
     * @param isCommon 是否是普通成员
     * @return 用户信息
     * @throws RuntimeException 无用户登录，不是管理员，不是普通用户
     */
    public static User getCurrent(
            @NotNull HttpSession session,
            UserService userService,
            boolean isManager,
            boolean isCommon) throws RuntimeException {
        Object o = session.getAttribute(UserService.USER_LOGIN_STATE);
        if (o == null) {
            throw new RuntimeException("当前用户未登录");
        }
        User user = (User) o;
        User getUser = userHelp(user.getId(), userService);
        checkUserRole(getUser, isManager, isCommon);
        checkUserInformation(getUser, user);
        return getUser;
    }

    /**
     * 根据题目删除其下的回复信息
     * @param session session域对象
     * @param topic 题目信息
     * @param replyService 回复服务类
     * @throws RuntimeException 删除回复失败
     */
    public static void removeReplyByTopic(
            HttpSession session,
            @NotNull Topic topic,
            @NotNull ReplyService replyService) throws RuntimeException {
        Long topicId = topic.getTopicId();
        QueryWrapper<Reply> wrapper = new QueryWrapper<>();
        wrapper.eq("topicId", topicId);
        List<Reply> topicReply = replyService.list(wrapper);
        if (!topicReply.isEmpty()) {
            replyService.deleteBatch(topicReply, session);
        }
        session.setAttribute("topicId:" + topicId, null);
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
            userService.sealUser(userIdRequest, getCurrent(session, userService));
        } else {
            userService.unsealUser(userIdRequest, getCurrent(session, userService));
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

    /**
     * 每隔一个月更新月积分
     * @param template redis模板类
     * @param userService 用户服务类
     * @return 是否更新标志
     * @throws RuntimeException 月积分更新失败
     */
    public static boolean monthlyChange(
            @NotNull RedisTemplate<String, Object> template,
            UserService userService) throws RuntimeException {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        String key = "suibe:mma:" + year + ":" + month;
        Boolean hasKey = template.hasKey(key);
        synchronized (SuibeMmaApplication.class) {
            if (hasKey == null || !hasKey) {
                synchronized (ControllerUtil.class) {
                    template
                            .opsForValue()
                            .set(key, year + "-" + month, c.getActualMaximum(Calendar.DAY_OF_MONTH), TimeUnit.DAYS);
                    String monthKey = key + ":monthScore";
                    HashOperations<String, Object, Object> operations = template.opsForHash();
                    userService.list().forEach(user -> {
                        Integer id = user.getId();
                        operations.put(monthKey, id.toString(), new UserIdAndMonthScore(id, user.getMonthScore()));
                    });
                    userService.monthScoreReset();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 每隔一年更新总积分
     * @param template redis模板类
     * @param userService 用户服务类
     * @return 是否更新标志
     * @throws RuntimeException 积分更新失败
     */
    public static boolean yearlyChange(
            @NotNull RedisTemplate<String, Object> template,
            UserService userService) throws RuntimeException {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        String key = "suibe:mma:" + year;
        Boolean hasKey = template.hasKey(key);
        synchronized (SuibeMmaApplication.class) {
            if (hasKey == null || !hasKey) {
                synchronized (ControllerUtil.class) {
                    template
                            .opsForValue()
                            .set(key, year, c.getActualMaximum(Calendar.DAY_OF_YEAR), TimeUnit.DAYS);
                    String yearKey = key + ":score";
                    HashOperations<String, Object, Object> operations = template.opsForHash();
                    userService.list().forEach(user -> {
                        Integer id = user.getId();
                        operations.put(yearKey, id.toString(), new UserIdAndScore(id, user.getScore()));
                    });
                    userService.scoreReset();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 根据积分重置请求获取用户对象信息
     * @param scoreSetRequest 积分重置请求类
     * @param userService 用户服务类
     * @return 用户信息
     * @throws RuntimeException 无用户
     */
    @NotNull
    public static User getByScoreRest(
            @NotNull ScoreSetRequest scoreSetRequest,
            @NotNull UserService userService) throws RuntimeException {
        Integer id = scoreSetRequest.getId();
        Integer userRole = scoreSetRequest.getUserRole();
        String userPassword = scoreSetRequest.getUserPassword();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper
                .eq("id", id)
                .eq("userRole", userRole)
                .eq("userPassword", encrypt(userPassword));
        User user = userService.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("传递信息有误");
        }
        return user;
    }
}
