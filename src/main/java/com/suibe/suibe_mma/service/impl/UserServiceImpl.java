package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.request.UserChangePasswordRequest;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.enumeration.UserExceptionEnumeration;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.suibe.suibe_mma.util.ServiceUtil.*;

/**
 * 用户服务类实现类
 */
@Service
@Transactional(rollbackFor = {UserException.class})
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
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
        if (checkUserAccount(userAccount) && checkUserPassword(userPassword)) {
            if (userPassword.equals(checkPassword)) {
                User user = new User();
                user.setUserAccount(userAccount);
                if (!isUserExist(user, userMapper)) {
                    user.setUserPassword(encrypt(userPassword));
                    if (!save(user)) {
                        UserExceptionEnumeration.USER_INSERT_FAILED.throwUserException();
                    }
                } else {
                    UserExceptionEnumeration.USER_ACCOUNT_EXISTS.throwUserException();
                }
            } else {
                UserExceptionEnumeration.USER_PASSWORD_NOT_EQUALS_CHECK_PASSWORD.throwUserException();
            }
        } else {
            UserExceptionEnumeration.USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG.throwUserException();
        }
    }

    @Override
    public User login(@NotNull UserLoginRequest userLoginRequest) throws UserException {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (checkUserAccount(userAccount) && checkUserPassword(userPassword)) {
            if (isUserExist(userAccount, userMapper)) {
                QueryWrapper<User> wrapper = new QueryWrapper<>();
                wrapper
                        .eq("userAccount", userAccount)
                        .eq("userPassword", encrypt(userPassword));
                User user = getOne(wrapper);
                if (user == null) {
                    UserExceptionEnumeration.USER_ACCOUNT_OR_PASSWORD_WRONG.throwUserException();
                }
                return user;
            } else {
                UserExceptionEnumeration.USER_ACCOUNT_NOT_EXISTS.throwUserException();
            }
        } else {
            UserExceptionEnumeration.USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG.throwUserException();
        }
        return null;
    }

    @Override
    public User checkCurrentUser(@NotNull User currentUser) throws UserException {
        Integer userId = currentUser.getId();
        User getUser = checkUserId(userId, this);
        if (!getUser.equals(currentUser)) {
            UserExceptionEnumeration.USER_INFORMATION_WRONG.throwUserException();
        }
        return currentUser;
    }

    @Override
    public User updateUserInfo(@NotNull User user) throws UserException {
        checkUserId(user.getId(), this);
        if (updateById(user)) {
            User newUser = getById(user.getId());
            return newUser;
        }
        UserExceptionEnumeration.USER_INFO_UPDATE_FAILED.throwUserException();
        return null;
    }

    @Override
    public User changeScore(@NotNull User user, Integer score) throws UserException {
        Integer id = user.getId();
        checkUserId(id, this);
        user.setScore(user.getScore() + score);
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        if (!update(user, wrapper)) {
            UserExceptionEnumeration.USER_SCORE_UPDATE_FAILED.throwUserException();
        }
        return user;
    }

    @Override
    public void changePassword(@NotNull UserChangePasswordRequest request) throws UserException {
        Integer id = request.getId();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        String newCheckPassword = request.getNewCheckPassword();
        if (checkUserPassword(oldPassword)) {
            if (checkUserPassword(newPassword)) {
                if (!oldPassword.equals(newPassword)) {
                    if (newPassword.equals(newCheckPassword)) {
                        checkUserId(id, this);
                        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
                        wrapper
                                .set("userPassword", encrypt(newPassword))
                                .setSql("updateTime = now()")
                                .eq("id", id)
                                .eq("userPassword", encrypt(oldPassword));
                        if (!update(wrapper)) {
                            UserExceptionEnumeration.USER_PASSWORD_CHANGE_FAILED.throwUserException();
                        }
                    } else {
                        UserExceptionEnumeration.USER_PASSWORD_NOT_EQUALS_CHECK_PASSWORD.throwUserException();
                    }
                } else {
                    UserExceptionEnumeration.USER_NEW_AND_OLD_PASSWORD_SAME.throwUserException();
                }
            } else {
                UserExceptionEnumeration.USER_NEW_PASSWORD_FORMAT_WRONG.throwUserException();
            }
        } else {
            UserExceptionEnumeration.USER_OLD_PASSWORD_FORMAT_WRONG.throwUserException();
        }
    }

}
