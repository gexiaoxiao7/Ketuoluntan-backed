package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.request.UserChangePasswordRequest;
import com.suibe.suibe_mma.domain.request.UserIdRequest;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.enumeration.UserEE;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.suibe.suibe_mma.util.DomainUtil.checkUserInformation;
import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 用户服务类实现类
 */
@Service
@Transactional
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {
    /**
     * 注入userMapper
     */
    @Resource
    private UserMapper userMapper;

    @Override
    public void register(@NotNull UserRegisterRequest userRegisterRequest) throws UserException {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (checkUserAccount(userAccount) || checkUserPassword(userPassword)) {
            UserEE.USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG.throwE();
        }
        if (!userPassword.equals(checkPassword)) {
            UserEE.USER_PASSWORD_NOT_EQUALS_CHECK_PASSWORD.throwE();
        }
        User user = new User();
        user.setUserAccount(userAccount);
        if (isUserExist(user, userMapper)) {
            UserEE.USER_ACCOUNT_EXISTS.throwE();
        }
        user.setUserPassword(encrypt(userPassword));
        if (!save(user)) {
            UserEE.USER_INSERT_FAILED.throwE();
        }
    }

    @Override
    public User login(@NotNull UserLoginRequest userLoginRequest) throws UserException {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (checkUserAccount(userAccount) || checkUserPassword(userPassword)) {
            UserEE.USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG.throwE();
        }
        if (!isUserExist(userAccount, userMapper)) {
            UserEE.USER_ACCOUNT_NOT_EXISTS.throwE();
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper
                .eq("userAccount", userAccount)
                .eq("userPassword", encrypt(userPassword));
        User user = getOne(wrapper);
        if (user == null) {
            UserEE.USER_ACCOUNT_OR_PASSWORD_WRONG.throwE();
        }
        return user;
    }

    @Override
    public User updateUserInfo(@NotNull User user) throws UserException {
        try {
            Integer id = user.getId();
            userHelp(id, this);
            user = notSetNull(user,
                    new String[]{"username", "avatarUrl", "gender", "email", "selfIntroduction"});
            if (!updateById(user)) {
                UserEE.USER_INFO_UPDATE_FAILED.throwE();
            }
            return getById(id);
        } catch (RuntimeException | IllegalAccessException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public User changeScore(
            @NotNull User user,
            Integer score) throws UserException {
        try {
            Integer id = user.getId();
            User getUser = userHelp(id, this);
            checkUserInformation(getUser, user);
            User user_plus = changeMonthScore(getUser, score);
            int score_plus = user_plus.getScore() + score;
            if (score_plus < 0) {
                score_plus = 0;
            }
            user_plus.setScore(score_plus);
            user_plus = notSetNull(user_plus, "score");
            if (!updateById(user_plus)) {
                UserEE.USER_SCORE_UPDATE_FAILED.throwE();
            }
            return getById(id);
        } catch (RuntimeException | IllegalAccessException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public User changePassword(
            @NotNull UserChangePasswordRequest request,
            User currentUser) throws UserException {
        try {
            Integer id = request.getId();
            String oldPassword = request.getOldPassword();
            String newPassword = request.getNewPassword();
            String newCheckPassword = request.getNewCheckPassword();
            String eOldPassword = encrypt(oldPassword);
            String eNewPassword = encrypt(newPassword);
            if (checkUserPassword(oldPassword)) {
                UserEE.USER_OLD_PASSWORD_FORMAT_WRONG.throwE();
            }
            if (checkUserPassword(newPassword)) {
                UserEE.USER_NEW_PASSWORD_FORMAT_WRONG.throwE();
            }
            if (eOldPassword.equals(eNewPassword)) {
                UserEE.USER_NEW_AND_OLD_PASSWORD_SAME.throwE();
            }
            if (!newPassword.equals(newCheckPassword)) {
                UserEE.USER_PASSWORD_NOT_EQUALS_CHECK_PASSWORD.throwE();
            }
            checkUserInformation(currentUser, this, false);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userPassword", eOldPassword).eq("id", id);
            if (getOne(queryWrapper) == null) {
                UserEE.USER_PASSWORD_WRONG.throwE();
            }
            UpdateWrapper<User> wrapper = new UpdateWrapper<>();
            wrapper
                    .set("userPassword", eNewPassword)
                    .setSql("updateTime = now()")
                    .eq("id", id);
            if (!update(wrapper)) {
                UserEE.USER_PASSWORD_CHANGE_FAILED.throwE();
            }
            return getById(id);
        } catch (RuntimeException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void sealUser(
            UserIdRequest userIdRequest,
            User currentUser) throws UserException {
        try {
            Integer userId = userIdRequest.getUserId();
            User user = userHelp(userId, this);
            if (currentUser.getId().equals(userId)) {
                throw new RuntimeException("不能给自己封号");
            }
            checkUserInformation(currentUser, this, false, true, false);
            UpdateWrapper<User> wrapper = new UpdateWrapper<>();
            wrapper
                    .eq("id", user.getId())
                    .set("userRole", 2)
                    .setSql("updateTime = now()");
            if (!update(wrapper)) {
                UserEE.USER_SEAL_FAILED.throwE();
            }
        } catch (RuntimeException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void unsealUser(
            UserIdRequest userIdRequest,
            User currentUser) throws UserException {
        try {
            User user = userHelp(userIdRequest.getUserId(), this, true);
            checkUserInformation(currentUser, this, false, true, false);
            UpdateWrapper<User> wrapper = new UpdateWrapper<>();
            wrapper
                    .eq("id", user.getId())
                    .set("userRole", 0)
                    .setSql("updateTime = now()");
            if (!update(wrapper)) {
                UserEE.USER_UNSEAL_FAILED.throwE();
            }
        } catch (RuntimeException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void giveManager(@NotNull User user) throws UserException {
        try {
            user.setUserRole(1);
            user = notSetNull(user, "userRole");
            if (!updateById(user)) {
                UserEE.USER_MANAGER_ROLE_CHANGE_FAILED.throwE();
            }
        } catch (IllegalAccessException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void recaptureManager(@NotNull User user) throws UserException {
        try {
            user.setUserRole(0);
            user = notSetNull(user, "userRole");
            if (!updateById(user)) {
                UserEE.USER_MANAGER_ROLE_CHANGE_FAILED.throwE();
            }
        } catch (IllegalAccessException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void monthScoreReset() throws UserException {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.set("monthScore", 0);
        if (!update(wrapper)) {
            UserEE.USER_SCORE_UPDATE_FAILED.throwE();
        }
    }

    @Override
    public void scoreReset() throws UserException {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.set("score", 0);
        if (!update(wrapper)) {
            UserEE.USER_SCORE_UPDATE_FAILED.throwE();
        }
    }

    /**
     * 更新用户月积分
     * @param user 用户信息
     * @param mouthScore 更新月积分
     * @return 更新后用户信息
     * @throws UserException 用户积分更新失败
     */
    private User changeMonthScore(
            @NotNull User user,
            Integer mouthScore) throws UserException {
        try {
            user.setMonthScore(user.getMonthScore() + mouthScore);
            user = notSetNull(user, "monthScore");
            if (!updateById(user)) {
                UserEE.USER_SCORE_UPDATE_FAILED.throwE();
            }
            return getById(user.getId());
        } catch (IllegalAccessException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

}
