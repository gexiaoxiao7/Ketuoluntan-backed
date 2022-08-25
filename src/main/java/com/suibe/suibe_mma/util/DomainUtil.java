package com.suibe.suibe_mma.util;

import com.suibe.suibe_mma.domain.Reply;
import com.suibe.suibe_mma.domain.Topic;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.enumeration.ReplyEE;
import com.suibe.suibe_mma.enumeration.TopicEE;
import com.suibe.suibe_mma.enumeration.UserEE;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;

import static com.suibe.suibe_mma.util.ServiceUtil.checkId;

/**
 * domain类帮助类
 */
public class DomainUtil {

    /**
     * 私有化构造方法
     */
    private DomainUtil() {}

    /**
     * 检查用户是否被封、是否为管理员、是否是普通用户
     * @param user 用户信息
     * @param isManager 是否是管理员
     * @param isCommon 是否是普通用户
     * @throws UserException 被封、不是管理员、不是普通用户
     */
    public static void checkUserRole(
            User user,
            boolean isManager,
            boolean isCommon) throws UserException {
        UserRole.checkUserRole(user, isManager, isCommon);
    }

    /**
     * 检查作者id是否匹配
     * @param topic 题目信息
     * @param userId 用户唯一标识
     * @param isAuthor 是否是作者标志
     * @throws TopicException 作者id不匹配
     */
    public static void checkTopicUserId(
            Topic topic,
            Integer userId,
            boolean isAuthor) throws TopicException {
        if (isAuthor && !topic.getUserId().equals(userId)) {
            TopicEE.TOPIC_USERID_MATCH_FAILED.throwE();
        }
    }

    /**
     * 检查作者id是否匹配
     * @param reply 回复信息
     * @param userId 用户唯一标识
     * @param isAuthor 是否是作者标志
     * @throws ReplyException 作者id不匹配
     */
    public static void checkReplyUserId(
            Reply reply,
            Integer userId,
            boolean isAuthor) throws ReplyException {
        if (isAuthor && !reply.getUserId().equals(userId)) {
            ReplyEE.REPLY_USERID_MATCH_FALIED.throwE();
        }
    }

    /**
     * 检查用户信息是否匹配
     * @param getUser 获取的用户信息
     * @param currentUser 当前用户信息
     * @throws UserException 信息不匹配
     */
    public static void checkUserInformation(
            @NotNull User getUser,
            User currentUser) throws UserException {
        if (!getUser.equals(currentUser)) {
            UserEE.USER_INFORMATION_WRONG.throwE();
        }
    }

    /**
     * 检查用户信息是否匹配
     * @param user 用户信息
     * @param userService 用户服务类
     * @param isUserSeal 用户是否被封标志
     * @param isManager 是否是管理员
     * @throws RuntimeException 用户被封，用户信息不匹配，id无效或为空，用户不是管理员，不是普通用户
     */
    public static void checkUserInformation(
            @NotNull User user,
            UserService userService,
            boolean isUserSeal,
            boolean isManager) throws RuntimeException {
        User getUser = checkId(User.class, user.getId(), userService);
        if (!isUserSeal) {
            checkUserRole(getUser, isManager, !isManager);
        } else {
            if (getUser.getUserRole() != 2) {
                UserEE.USER_NOT_SEAL.throwE();
            }
        }
        checkUserInformation(getUser, user);
    }

    /**
     * 检查用户信息是否匹配
     * @param user 用户信息
     * @param userService 用户服务类
     * @param isUserSeal 用户是否被封标志
     * @throws RuntimeException 用户被封，用户信息不匹配，id无效或为空
     */
    public static void checkUserInformation(
            @NotNull User user,
            UserService userService,
            boolean isUserSeal) throws RuntimeException {
        checkUserInformation(user, userService, isUserSeal, false);
    }

    /**
     * 用户身份类
     */
    private enum UserRole {
        /**
         * 普通用户
         */
        COMMON_USER(0),
        /**
         * 管理员用户
         */
        MANAGER_USER(1),
        /**
         * 被封用户
         */
        SEALED_USER(2);

        /**
         * 用户身份数字
         */
        private final Integer roleNum;

        /**
         * 身份数字构造方法
         * @param roleNum 身份数字
         */
        UserRole(Integer roleNum) {
            this.roleNum = roleNum;
        }

        /**
         * 检查用户是否被封或是否为管理员
         * @param user 用户信息
         * @param isManager 是否是管理员
         * @throws RuntimeException 被封、不是管理员
         */
        private static void checkUserRole(
                @NotNull User user,
                boolean isManager,
                boolean isCommon) throws UserException{
            if (user.getUserRole().equals(SEALED_USER.roleNum)) {
                UserEE.USER_SEALED.throwE();
            }
            if (isManager && !user.getUserRole().equals(MANAGER_USER.roleNum)) {
                UserEE.USER_NOT_MANAGER.throwE();
            }
            if (isCommon && !user.getUserRole().equals(COMMON_USER.roleNum)) {
                UserEE.USER_NOT_NORMAL.throwE();
            }
        }
    }
}
