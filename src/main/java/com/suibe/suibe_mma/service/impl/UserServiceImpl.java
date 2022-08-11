package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        return getSafetyUser(currentUser);
    }

    @Override
    public User updateUserInfo(@NotNull User user) throws UserException {
        checkUserId(user.getId(), this);
        if (updateById(user)) {
            return getSafetyUser(user);
        }
        UserExceptionEnumeration.USER_INFO_UPDATE_FAILED.throwUserException();
        return null;
    }

    @Override
    public User changeScore(User user, Integer score) throws UserException {
        User safetyUser = getSafetyUser(user);
        safetyUser.setScore(safetyUser.getScore() + score);
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", safetyUser.getId());
        if (!update(safetyUser, wrapper)) {
            UserExceptionEnumeration.USER_SCORE_UPDATE_FAILED.throwUserException();
        }
        return safetyUser;
    }

}
