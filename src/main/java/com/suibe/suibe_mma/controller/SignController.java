package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.Message;
import com.suibe.suibe_mma.domain.Sign;
import com.suibe.suibe_mma.domain.StringResponse;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.MessageIdRequest;
import com.suibe.suibe_mma.domain.request.SignIdRequest;
import com.suibe.suibe_mma.domain.request.SignReportRequest;
import com.suibe.suibe_mma.service.SignService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.suibe.suibe_mma.util.ControllerUtil.getCurrent;
import static com.suibe.suibe_mma.util.ControllerUtil.requestFail;

@RestController
@RequestMapping("/sign")
public class SignController {
    /**
     * 注入messageService
     */
    @Resource
    private SignService signService;

    /**
     * 注入userService
     */
    @Resource
    private UserService userService;


    /**
     * 获取接收的所有信息
     * @param request 请求域对象
     * @return 信息列表
     */
    @PostMapping("/getReceiveSigns")
    public List<Sign> getReceiveMessages(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Sign> signs = signService.getReceiveSigns(current);
            session.setAttribute("errMsg", null);
            return signs;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 查看信息内容
     * @param request 请求域对象
     * @param signIdRequest 信息唯一标识请求类
     * @return 信息具体内容
     */
    @PostMapping("/read")
    public Sign read(
            @NotNull HttpServletRequest request,
            @RequestBody SignIdRequest signIdRequest) {
        HttpSession session = request.getSession();
        try {
            requestFail(signIdRequest);
            User current = getCurrent(session, userService);
            Sign sign = signService.read(signIdRequest, current);
            session.setAttribute("errMsg", null);
            return sign;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 用户向管理员报名
     * @param request 请求域对象
     * @param signReportRequest 举报信息请求类
     * @return 提示信息
     */
    @PostMapping("/signReport")
    public StringResponse signReport(
            @NotNull HttpServletRequest request,
            @RequestBody SignReportRequest signReportRequest) {
        HttpSession session = request.getSession();
        StringResponse stringResponse = new StringResponse();
        try {
            requestFail(signReportRequest);
            User current = getCurrent(session, userService);
            signService.signReport(signReportRequest, current);
            stringResponse.setMessage("提交成功");
            return stringResponse;
        } catch (RuntimeException e) {
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
        }
    }

    /**
     * 删除信息
     * @param request 请求域对象
     * @param sign 信息类
     * @return 提示信息
     */
    @PostMapping("/delete")
    public StringResponse delete(
            @NotNull HttpServletRequest request,
            @RequestBody Sign sign) {
        HttpSession session = request.getSession();
        StringResponse stringResponse = new StringResponse();
        try {
            requestFail(sign);
            User current = getCurrent(session, userService);
            signService.delete(current, sign);
            stringResponse.setMessage("删除成功");
            return stringResponse;
        } catch (RuntimeException e) {
            stringResponse.setMessage(e.getMessage());
            return stringResponse;
        }
    }

    /**
     * 获取收者已读信息列表
     * @param request 请求域对象
     * @return 信息列表
     */
    @PostMapping("/getRead")
    public List<Sign> getRead(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Sign> read = signService.getReadOrUnRead(current, true);
            session.setAttribute("errMsg", null);
            return read;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

    /**
     * 获取收者信未读信息列表
     * @param request 请求域对喜感
     * @return 信息列表
     */
    @PostMapping("/getUnRead")
    public List<Sign> getUnRead(@NotNull HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            User current = getCurrent(session, userService);
            List<Sign> unRead = signService.getReadOrUnRead(current, false);
            session.setAttribute("errMsg", null);
            return unRead;
        } catch (RuntimeException e) {
            session.setAttribute("errMsg", e.getMessage());
            return null;
        }
    }

}
