package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.*;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.suibe.suibe_mma.util.ControllerUtil.*;
import static com.suibe.suibe_mma.util.DomainUtil.checkUserRole;
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
        try {
            requestFail(userRegisterRequest);
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
            requestFail(userLoginRequest);
            User user = userService.login(userLoginRequest);
            checkUserRole(user, false, false);
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
     * @param userIdRequest 用户id类
     * @param request 请求域对象
     * @return 用户信息
     */
    @PostMapping("/searchByUserId")
    public User searchByUserId(
            @RequestBody UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(userIdRequest);
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
            requestFail(user);
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
     * @param request 请求域对象
     * @return 提示信息
     */
    @PostMapping("/changePassword")
    public String changePassword(
            @RequestBody UserChangePasswordRequest userChangePasswordRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(userChangePasswordRequest);
            session.setAttribute(
                    UserService.USER_LOGIN_STATE,
                    userService.changePassword(userChangePasswordRequest, getCurrent(session))
            );
            return "修改密码成功";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    /**
     * 管理员封号用户
     * @param userIdRequest 用户id类
     * @param request 请求域对象
     * @return 用户id
     */
    @PostMapping("/sealUser")
    public Integer sealUser(
            @RequestBody UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            sealUserOrNot(userIdRequest, session, userService, true);
            session.setAttribute("errMsg", null);
            return userIdRequest.getUserId();
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 管理员解封用户
     * @param userIdRequest 用户id类
     * @param request 请求域对象
     * @return 用户id
     */
    @PostMapping("/unsealUser")
    public Integer unsealUser(
            @RequestBody UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            sealUserOrNot(userIdRequest, session, userService, false);
            session.setAttribute("errMsg", null);
            return userIdRequest.getUserId();
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 管理员给用户修改积分
     * @param changeScoreRequest 修改积分请求类
     * @param request 请求域对象
     * @return 用户id
     */
    @PostMapping("/scoreChange")
    public Integer scoreChange(
            @RequestBody ChangeScoreRequest changeScoreRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(changeScoreRequest);
            User current = getCurrent(session);
            checkUserRole(current, true, false);
            Integer id = changeScoreRequest.getId();
            if (current.getId().equals(id)) {
                throw new RuntimeException("不能为自己改变分数");
            }
            userService.managerChangeScore(checkId(User.class, id, userService), changeScoreRequest.getScore());
            session.setAttribute("errMsg", null);
            return id;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    @PostMapping("/giveManager")
    public Integer giveManager(
            UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(userIdRequest);
            User current = getCurrent(session);
            checkUserRole(current, true, false);
            User user = checkId(User.class, userIdRequest.getUserId(), userService);
            checkUserRole(user, false, true);
            userService.giveManager(user);
            session.setAttribute("errMsg", null);
            return null;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }
}
