package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
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
}
