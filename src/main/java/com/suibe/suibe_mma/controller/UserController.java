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

@RestController
@RequestMapping("/user")
public class UserController {
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
        User getUser = userService.getById(currentUser.getId());
        try {
            return userService.checkCurrentUser(getUser, currentUser);
        } catch (UserException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }
}
