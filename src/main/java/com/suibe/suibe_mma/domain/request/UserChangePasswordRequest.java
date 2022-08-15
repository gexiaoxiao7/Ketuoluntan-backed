package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户修改密码信息类
 */
@Data
public class UserChangePasswordRequest
        implements Serializable {

    /**
     * 用户唯一标识
     */
    private Integer id;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 新密码校验
     */
    private String newCheckPassword;

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;
}
