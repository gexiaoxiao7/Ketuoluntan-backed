package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScoreSetRequest
        implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private Integer id;

    /**
     * 用户角色
     */
    private Integer userRole;

    /**
     * 用户密码
     */
    private String userPassword;
}
