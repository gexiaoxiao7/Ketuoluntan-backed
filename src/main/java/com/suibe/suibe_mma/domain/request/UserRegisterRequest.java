package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册信息类
 */
@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户注册账户名
     */
    private String userAccount;

    /**
     * 用户注册密码
     */
    private String userPassword;

    /**
     * 用户注册校验码
     */
    private String checkPassword;
}
