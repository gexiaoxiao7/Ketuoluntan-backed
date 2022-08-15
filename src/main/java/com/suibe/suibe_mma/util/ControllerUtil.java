package com.suibe.suibe_mma.util;

import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpSession;

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
        return (User) o;
    }
}
