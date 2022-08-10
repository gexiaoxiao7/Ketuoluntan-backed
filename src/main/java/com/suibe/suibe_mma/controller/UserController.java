package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
    public String userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
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
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (userLoginRequest == null) {
            session.setAttribute(userService.USER_LOGIN_STATE, null);
            session.setAttribute("errMsg", "请求失败");
            return null;
        }
        try {
            User user = userService.login(userLoginRequest);
            session.setAttribute(userService.USER_LOGIN_STATE, user);
            session.setAttribute("errMsg", null);
            return user;
        } catch (UserException e) {
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
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object originUser = session.getAttribute(UserService.USER_LOGIN_STATE);
        if (originUser == null) {
            session.setAttribute("errMsg", "无用户登录");
            return null;
        }
        User currentUser = (User) originUser;
        try {
            User safetyUser = userService.checkCurrentUser(currentUser);
            session.setAttribute("errMsg", null);
            return safetyUser;
        } catch (UserException e) {
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
    public User updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (user == null) {
            session.setAttribute("errMsg", "请求失败");
            return null;
        }
        User originUser = (User) session.getAttribute(UserService.USER_LOGIN_STATE);
        if (originUser == null) {
            session.setAttribute("errMsg", "用户未登录");
            return null;
        }
        if (originUser.equals(user)) {
            session.setAttribute("errMsg", "用户信息无变动");
            return null;
        }
        if (user.getUserPassword() != null) {
            session.setAttribute("errMsg", "该功能不提供修改密码需求");
            return null;
        }
        try {
            User userInfo = userService.updateUserInfo(user);
            session.setAttribute("errMsg", null);
            session.setAttribute(UserService.USER_LOGIN_STATE, userInfo);
            return userInfo;
        } catch (UserException e) {
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
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (getCurrentUser(request) == null) {
            return "用户未登录";
        }
        session.setAttribute(UserService.USER_LOGIN_STATE, null);
        session.setAttribute("errMsg", null);
        return "用户已退出";
    }
}
