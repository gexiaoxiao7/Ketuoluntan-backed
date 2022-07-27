package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = -6711190183287286640L;

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
