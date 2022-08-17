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
import static com.suibe.suibe_mma.util.DomainUtil.checkUserRole;
import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 用户服务类实现类
 */
@Service
@Transactional(rollbackFor = {UserException.class})
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
    public User checkCurrentUser(@NotNull User currentUser) throws UserException {
        try {
            Integer userId = currentUser.getId();
            User getUser = checkId(User.class, userId, this);
            checkUserInformation(getUser, currentUser);
            return currentUser;
        }  catch (RuntimeException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public User updateUserInfo(@NotNull User user) throws UserException {
        try {
            Integer id = user.getId();
            checkId(User.class, id, this);
            user.setUpdateTime(null);
            if (!updateById(user)) {
                UserEE.USER_INFO_UPDATE_FAILED.throwE();
            }
            return getById(id);
        } catch (RuntimeException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public User changeScore(
            @NotNull User user,
            Integer score) throws UserException {
        try {
            Integer id = user.getId();
            checkId(User.class, id, this);
            user.setScore(user.getScore() + score);
            user.setUpdateTime(null);
            UpdateWrapper<User> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", id);
            if (!update(user, wrapper)) {
                UserEE.USER_SCORE_UPDATE_FAILED.throwE();
            }
            return user;
        } catch (RuntimeException e) {
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
            if (checkUserPassword(oldPassword)) {
                UserEE.USER_OLD_PASSWORD_FORMAT_WRONG.throwE();
            }
            if (checkUserPassword(newPassword)) {
                UserEE.USER_NEW_PASSWORD_FORMAT_WRONG.throwE();
            }
            if (oldPassword.equals(newPassword)) {
                UserEE.USER_NEW_AND_OLD_PASSWORD_SAME.throwE();
            }
            if (!newPassword.equals(newCheckPassword)) {
                UserEE.USER_PASSWORD_NOT_EQUALS_CHECK_PASSWORD.throwE();
            }
            checkUserInformation(checkId(User.class, id, this), currentUser);
            UpdateWrapper<User> wrapper = new UpdateWrapper<>();
            wrapper
                    .set("userPassword", encrypt(newPassword))
                    .setSql("updateTime = now()")
                    .eq("id", id)
                    .eq("userPassword", encrypt(oldPassword));
            if (!update(wrapper)) {
                UserEE.USER_PASSWORD_CHANGE_FAILED.throwE();
            }
            return getById(id);
        } catch (RuntimeException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void sealUser(UserIdRequest userIdRequest, User currentUser) throws UserException {
        try {
            User user = checkId(User.class, userIdRequest.getUserId(), this);
            checkUserInformation(checkId(User.class, currentUser.getId(), this), currentUser);
            checkUserRole(currentUser, true);
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
    public void unsealUser(UserIdRequest userIdRequest, User currentUser) throws UserException {
        try {
            User user = checkId(User.class, userIdRequest.getUserId(), this);
            checkUserInformation(checkId(User.class, currentUser.getId(), this), currentUser);
            checkUserRole(currentUser, true);
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

}
