package com.suibe.suibe_mma.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suibe.suibe_mma.SuibeMmaApplication;
import com.suibe.suibe_mma.domain.StringResponse;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.*;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.List;

import static com.suibe.suibe_mma.util.ControllerUtil.*;
import static com.suibe.suibe_mma.util.DomainUtil.checkUserInformation;
import static com.suibe.suibe_mma.util.DomainUtil.checkUserRole;
import static com.suibe.suibe_mma.util.ServiceUtil.checkId;
import static com.suibe.suibe_mma.util.ServiceUtil.userHelp;

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
     * 注入redisTemplate
     */
    @Resource
    private RedisTemplate<String, Object> template;

    /**
     * 用户注册
     * 如果有错误则直接返回错误信息，
     * 无错误则返回"注册成功"
     * @param userRegisterRequest 用户注册信息
     * @return 相关信息
     */
    @PostMapping("/register")
    public StringResponse register(@RequestBody UserRegisterRequest userRegisterRequest) {
        StringResponse stringResponse = new StringResponse();
        try {
            requestFail(userRegisterRequest);
            userService.register(userRegisterRequest);
            stringResponse.setMessage("注册成功");
            return stringResponse;
        } catch (RuntimeException e) {
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
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
            if (monthlyChange(template, userService) | yearlyChange(template, userService))
                user = userService.getById(user.getId());
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
            User current = getCurrent(session, userService);
            session.setAttribute(UserService.USER_LOGIN_STATE, current);
            session.setAttribute("errMsg", null);
            return current;
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
            User originUser = getCurrent(session, userService);
            if (originUser.equals(user)) {
                throw new RuntimeException("用户信息无变动");
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
            getCurrent(session, userService);
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
    public StringResponse changePassword(
            @RequestBody UserChangePasswordRequest userChangePasswordRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(userChangePasswordRequest);
            session.setAttribute(
                    UserService.USER_LOGIN_STATE,
                    userService.changePassword(userChangePasswordRequest, getCurrent(session, userService))
            );
            StringResponse stringResponse = new StringResponse();
            stringResponse.setMessage("修改密码成功");
            return stringResponse;
        } catch (RuntimeException e) {
            StringResponse stringResponse = new StringResponse();
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
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
        synchronized (SuibeMmaApplication.class) {
            try {
                requestFail(changeScoreRequest);
                User current = getCurrent(session, userService, true, false);
                Integer id = changeScoreRequest.getId();
                if (current.getId().equals(id)) {
                    throw new RuntimeException("不能为自己改变分数");
                }
                userService.changeScore(userHelp(id, userService), changeScoreRequest.getScore());
                session.setAttribute("errMsg", null);
                return id;
            } catch (RuntimeException e) {
                session.setAttribute("errMsg", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 赋予管理员权限
     * @param userIdRequest 用户id类
     * @param request 请求域对象
     * @return 用户id
     */
    @PostMapping("/giveManager")
    public Integer giveManager(
            @RequestBody UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(userIdRequest);
            getCurrent(session, userService, true, false);
            User user = userHelp(userIdRequest.getUserId(), userService);
            userService.giveManager(user);
            session.setAttribute("errMsg", null);
            return user.getId();
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 总排名
     * @param request 请求域对象
     * @return 用户信息列表
     */
    @PostMapping("/getAllUsers")
    public List<User> getAllUsers(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            getCurrent(session, userService);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper
//                    .orderByDesc("score")
                    .orderByAsc("createTime");
            session.setAttribute("errMsg", null);
            return userService.list(wrapper);
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 月排名
     * @param request 请求域对象
     * @return 用户信息列表
     */
    @PostMapping("/getAllUsersByMonth")
    public List<User> getAllUsersByMonth(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            getCurrent(session, userService);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper
                    .orderByDesc("monthScore")
                    .orderByAsc("createTime");
            session.setAttribute("errMsg", null);
            return userService.list(wrapper);
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 取消管理员权限
     * @param userIdRequest 用户id类
     * @param request 请求域对象
     * @return 用户id
     */
    @PostMapping("/recaptureManager")
    public Integer recaptureManager(
            @RequestBody UserIdRequest userIdRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(userIdRequest);
            User current = getCurrent(session, userService, true, false);
            User user = userHelp(userIdRequest.getUserId(), userService, true, false);
            if (user.getId().equals(current.getId())) {
                throw new RuntimeException("不能取消自己的管理员权限");
            }
            userService.recaptureManager(user);
            session.setAttribute("errMsg", null);
            return user.getId();
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 管理员手动重置月积分
     * @param scoreSetRequest 积分重置请求类
     * @param request 请求域对象
     * @return 提示信息
     */
    @PostMapping("/monthReset")
    public StringResponse monthReset(
            @RequestBody ScoreSetRequest scoreSetRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(scoreSetRequest);
            User getUser = getByScoreReset(scoreSetRequest, userService);
            User current = getCurrent(session, userService, true, false);
            checkUserInformation(getUser, current);
            StringResponse stringResponse = new StringResponse();
            if (monthlyChange(template, userService)) {
                session.setAttribute(UserService.USER_LOGIN_STATE, userService.getById(current.getId()));
                stringResponse.setMessage("重置月积分成功");
                return stringResponse;
            }
            stringResponse.setMessage("月积分已经重置过了");
            return stringResponse;
        } catch (RuntimeException e) {
            StringResponse stringResponse = new StringResponse();
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
        }
    }

    /**
     * 管理员手动重置总积分
     * @param request 请求域对象
     * @return 提示信息
     */
    @PostMapping("/scoreReset")
    public StringResponse scoreReset(
            @RequestBody ScoreSetRequest scoreSetRequest,
            @NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            requestFail(scoreSetRequest);
            User getUser = getByScoreReset(scoreSetRequest, userService);
            User current = getCurrent(session, userService, true, false);
            checkUserInformation(getUser, current);
            StringResponse stringResponse = new StringResponse();
            if (yearlyChange(template, userService)) {
                session.setAttribute(UserService.USER_LOGIN_STATE, userService.getById(current.getId()));
                stringResponse.setMessage("重置总积分成功");
                return stringResponse;
            }
            stringResponse.setMessage("总积分已经重置过了");
            return stringResponse;
        } catch (RuntimeException e) {
            StringResponse stringResponse = new StringResponse();
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
        }
    }
}

