package com.suibe.suibe_mma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibe.suibe_mma.domain.request.UserLoginRequest;
import com.suibe.suibe_mma.domain.request.UserRegisterRequest;
import com.suibe.suibe_mma.enumeration.UserExceptionEnumeration;
import com.suibe.suibe_mma.exception.UserException;
import com.suibe.suibe_mma.mapper.UserMapper;
import com.suibe.suibe_mma.domain.User;
import com.suibe.suibe_mma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor = {UserException.class})
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(UserRegisterRequest userRegisterRequest) throws UserException {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (checkUserAccount(userAccount) && checkUserPassword(userPassword)) {
            if (userPassword.equals(checkPassword)) {
                User user = new User();
                user.setUserAccount(userAccount);
                if (!isUserExist(user)) {
                    user.setUserPassword(encrypt(userPassword));
                    if (!save(user)) {
                        throw UserException.getInstance(UserExceptionEnumeration.USER_INSERT_FAILED);
                    }
                } else {
                    throw UserException.getInstance(UserExceptionEnumeration.USER_ACCOUNT_EXISTS);
                }
            } else {
                throw UserException.getInstance(UserExceptionEnumeration.USER_PASSWORD_NOT_EQUALS_CHECKPASSWORD);
            }
        } else {
            throw UserException.getInstance(UserExceptionEnumeration.USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG);
        }
    }

    @Override
    public User login(UserLoginRequest userLoginRequest) throws UserException {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (checkUserAccount(userAccount) || checkUserPassword(userPassword)) {
            if (isUserExist(userAccount)) {
                QueryWrapper<User> wrapper = new QueryWrapper<>();
                wrapper.eq("userAccount", userAccount);
                wrapper.eq("userPassword", encrypt(userPassword));
                List<User> list = list(wrapper);
                if (list.isEmpty()) {
                    throw UserException.getInstance(UserExceptionEnumeration.USER_ACCOUNT_OR_PASSWORD_WRONG);
                }
                return getSafetyUser(list.get(0));
            } else {
                throw UserException.getInstance(UserExceptionEnumeration.USER_ACCOUNT_NOT_EXISTS);
            }
        } else {
            throw UserException.getInstance(UserExceptionEnumeration.USER_ACCOUNT_OR_PASSWORD_FORMAT_WRONG);
        }
    }

    /**
     * 使用md5加密
     * @param message 加密信息
     * @return 加密后的信息
     */
    private String encrypt(String message) {
        return DigestUtils.md5DigestAsHex((SALT + message + SALT).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 检验账户名格式
     * 不为null和""
     * 长度为8
     * 不能有数字之外的值
     * @param userAccount 账户名信息
     * @return 是否符合
     */
    private boolean checkUserAccount(String userAccount) {
        if (userAccount == null || "".equals(userAccount)) {
            return false;
        }
        if (userAccount.length() != 8) {
            return false;
        }
        return !Pattern.compile(".*[^0-9].*").matcher(userAccount).find();
    }

    /**
     * 检验密码格式
     * 不为null和""
     * 长度大于等于8
     * 以字母开头
     * @param userPassword 密码信息
     * @return 是否符合
     */
    private boolean checkUserPassword(String userPassword) {
        if (userPassword == null || "".equals(userPassword)) {
            return false;
        }
        if (userPassword.length() < 8) {
            return false;
        }
        return userPassword.matches("[a-zA-Z].*");
    }

    /**
     * 检验用户是否存在
     * @param user 用户信息
     * @return 是否存在
     */
    private boolean isUserExist(User user) {
        return userMapper.selectByUserAccount(user.getUserAccount()) > 0;
    }

    /**
     * 检验用户是否存在
     * @param userAccount 账户名
     * @return 是否存在
     */
    private boolean isUserExist(String userAccount) {
        return userMapper.selectByUserAccount(userAccount) > 0;
    }

    /**
     * 获取安全用户信息
     * @param originUser 原始用户信息
     * @return 安全用户信息
     */
    private User getSafetyUser(User originUser) {
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setScore(originUser.getScore());
        safetyUser.setUserRole(originUser.getUserRole());
        return safetyUser;
    }
}
