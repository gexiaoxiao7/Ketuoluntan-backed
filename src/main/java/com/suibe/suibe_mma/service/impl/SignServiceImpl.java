package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.Message;
import com.suibe.suibe_mma.domain.Sign;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.domain.request.MessageIdRequest;
import com.suibe.suibe_mma.domain.request.SignIdRequest;
import com.suibe.suibe_mma.domain.request.SignReportRequest;
import com.suibe.suibe_mma.enumeration.MessageEE;
import com.suibe.suibe_mma.exception.MessageException;
import com.suibe.suibe_mma.exception.SignException;
import com.suibe.suibe_mma.mapper.SignMapper;
import com.suibe.suibe_mma.service.SignService;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.suibe.suibe_mma.util.ServiceUtil.*;
import static com.suibe.suibe_mma.util.ServiceUtil.messageDelete;

/**
 * 报名服务实现类
 */
@Service
@Transactional
public class SignServiceImpl
        extends ServiceImpl<SignMapper, Sign>
        implements SignService {

    /**
     * 注入userService
     */
    @Resource
    private UserService userService;

    @Override
    public List<Sign> getReceiveSigns(@NotNull User current) {
        QueryWrapper<Sign> wrapper = new QueryWrapper<>();
        wrapper
                .eq("receiveId", current.getId())
                .eq("receiveDelete", false);
        return list(wrapper);
    }

    @Override
    public void signReport(
            @NotNull SignReportRequest signReportRequest,
            @NotNull User current) throws SignException {
        try {
            Integer sendId = signReportRequest.getSendId();
            String signName = signReportRequest.getSignName();
            String signSId = signReportRequest.getSignSId();
            String qqId = signReportRequest.getQqId();
            String majorClass = signReportRequest.getMajorClass();
            String phoneNumber = signReportRequest.getPhoneNumber();
            String email = signReportRequest.getEmail();
            String experience = signReportRequest.getExperience();
            String reasons = signReportRequest.getReasons();
            userHelp(sendId, userService);
            if (!sendId.equals(current.getId())) {
                MessageEE.MESSAGE_NOT_CURRENT.throwE();
            }
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userRole", 1);
            List<User> managers = userService.list(queryWrapper);

            managers.stream().filter(user -> !user.getId().equals(sendId)).forEach(user -> {
                Sign sign = new Sign();
                sign.setSendId(sendId);
                sign.setSignName(signName);
                sign.setSignSId(signSId);
                sign.setQqId(qqId);
                sign.setMajorClass(majorClass);
                sign.setPhoneNumber(phoneNumber);
                sign.setEmail(email);
                sign.setExperience(experience);
                sign.setReasons(reasons);
                sign.setReceiveId(user.getId());
                if (!save(sign)) {
                    MessageEE.MESSAGE_SAVE_FAILED.throwE();
                }
            });
        } catch (RuntimeException e) {
            throw new MessageException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(
            User current,
            @NotNull Sign sign) throws SignException {
        try {
            Integer id = current.getId();
            Sign getSign = checkId(Sign.class, sign.getSignId(), this);
            boolean receiveFlag = getSign.getReceiveId().equals(id);
            boolean receiveDelete = getSign.getReceiveDelete();
            signDelete(getSign, receiveFlag, receiveDelete, "sendDelete", this);
        } catch (RuntimeException e) {
            throw new MessageException(e.getMessage(), e);
        }
    }

    @Override
    public List<Sign> getReadOrUnRead(
            @NotNull User current,
            boolean isRead) {
        QueryWrapper<Sign> wrapper = new QueryWrapper<>();
        wrapper
                .eq("receiveId", current.getId())
                .eq("isRead", isRead)
                .eq("receiveDelete", false);
        return list(wrapper);
    }

    @Override
    public Sign read(
            @NotNull SignIdRequest signIdRequest,
            User current) throws SignException {
        try {
            Integer signId = signIdRequest.getSignId();
            Sign sign = checkId(Sign.class, signId, this);
            Integer id = current.getId();
            boolean sendFlag = id.equals(sign.getSendId());
            boolean receiveFlag = id.equals(sign.getReceiveId());
            if (receiveFlag && !sign.getReceiveDelete()) {
                if (!sign.getIsRead()) {
                    sign.setIsRead(true);
                    notSetNull(sign, "isRead");
                    if (!updateById(sign)) {
                        MessageEE.MESSAGE_READ_UPDATE_FAILED.throwE();
                    }
                    sign = getById(signId);
                }
            } else if (receiveFlag) {
                MessageEE.MESSAGE_DELETE.throwE();
            }
            if (!sendFlag && !receiveFlag) {
                MessageEE.MESSAGE_UNABLE_READ.throwE();
            }
            return sign;
        } catch (RuntimeException e) {
            throw new MessageException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

