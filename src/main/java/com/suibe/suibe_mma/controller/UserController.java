package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.UserChangePasswordRequest;
import com.suibe.suibe_mma.domain.request.UserIdRequest;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.suibe.suibe_mma.util.ControllerUtil.getCurrent;
import static com.suibe.suibe_mma.util.ServiceUtil.checkId;

/**
 * 用户相关操作控制类
 */
@RestController
@RequestMapping("/user")
public class UserController {
    /**
     * 注入UserService
     */
    @Resource
    private UserService userService;

    /**
     * 用户注册
     * 如果有错误则直接返回错误信息，
     * 无错误则返回"注册成功"
     * @param userRegisterRequest 用户注册信息
     * @return 相关信息
     */
    @PostMapping("/register")
    public String register(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return "请求失败";
        }
        try {
            userService.register(userRegisterRequest);
            return "注册成功";
        } catch (UserException e) {
            return e.getMessage();
        }
    }

    /**
     * 用户登录
     * 如果有错误则在Session域中设置错误信息
     * @param userLoginRequest 用户登录信息
     * @param request 请求域对象
     * @return 用户安全信息
     */
    @PostMapping("/login")
    public User login(
            @RequestBody UserLoginRequest userLoginRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (userLoginRequest == null) {
                throw new RuntimeException("请求失败");
            }
            User user = userService.login(userLoginRequest);
            if (user.getUserRole() == 2) {
                throw new RuntimeException("该用户已被封号");
            }
            session.setAttribute(userService.USER_LOGIN_STATE, user);
            session.setAttribute("errMsg", null);
            return user;
        } catch (RuntimeException e) {
            session.setAttribute(userService.USER_LOGIN_STATE, null);
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前登录用户
     * 如果用户信息不一致或根据id查询不到会将错误保存到session域中
     * @param request 请求域对象
     * @return 用户信息
     */
    @GetMapping("/current")
    public User getCurrentUser(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User safetyUser = userService.checkCurrentUser(getCurrent(session));
            session.setAttribute("errMsg", null);
            return safetyUser;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 根据用户ID当获取用户信息
     * @return 用户信息
     */
    @PostMapping("/searchByUserId")
    public User searchByUserId(
            @RequestBody UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (userIdRequest == null) {
                throw new RuntimeException("用户id传递失败");
            }
            User user = checkId(User.class, userIdRequest.getUserId(), userService);
            session.setAttribute("errMsg", null);
            return user;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 用户信息更新
     * @param user 需要更新的用户信息
     * @param request 请求域对象
     * @return 更新后的用户信息
     */
    @PostMapping("/update")
    public User updateUserInfo(
            @RequestBody User user,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (user == null) {
                throw new RuntimeException("请求失败");
            }
            User originUser = getCurrent(session);
            if (originUser.equals(user)) {
                throw new RuntimeException("用户信息无变动");
            }
            if (!originUser.getUserRole().equals(user.getUserRole())) {
                throw new RuntimeException("该功能不提供修改用户角色需求");
            }
            if (user.getUserPassword() != null) {
                throw new RuntimeException("该功能不提供修改密码需求");
            }
            User userInfo = userService.updateUserInfo(user);
            session.setAttribute("errMsg", null);
            session.setAttribute(UserService.USER_LOGIN_STATE, userInfo);
            return userInfo;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 用户退出登录
     * @param request 请求域对象
     * @return 提示消息
     */
    @GetMapping("/logout")
    public String logout(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            getCurrent(session);
            session.setAttribute(UserService.USER_LOGIN_STATE, null);
            session.setAttribute("errMsg", null);
            return "用户退出成功";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    /**
     * 更改密码
     * @param userChangePasswordRequest 更改密码信息实例
     * @return 提示信息
     */
    @PostMapping("/changePassword")
    public String changePassword(
            @RequestBody UserChangePasswordRequest userChangePasswordRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (userChangePasswordRequest == null) {
                return "请求失败";
            }
            getCurrent(session);
            session.setAttribute(
                    UserService.USER_LOGIN_STATE,
                    userService.changePassword(userChangePasswordRequest)
            );
            return "修改密码成功";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/sealUser")
    public Integer sealUser(
            @RequestBody UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            if (userIdRequest == null) {
                throw new RuntimeException("用户id传递失败");
            }
            if (getCurrent(session).getUserRole() != 1) {
                throw new RuntimeException("当前用户不为管理员，操作失败");
            }
            userService.sealUser(userIdRequest);
            session.setAttribute("errMsg", null);
            return userIdRequest.getUserId();
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }
}
