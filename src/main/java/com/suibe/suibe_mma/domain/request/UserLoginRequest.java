package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录信息类
 */
@Data
public class UserLoginRequest implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 2085298263565334420L;

    /**
     * 用户登录账户名
     */
    private String userAccount;

    /**
     * 用户登录密码
     */
    private String userPassword;
}
