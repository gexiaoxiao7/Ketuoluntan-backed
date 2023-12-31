package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 传递用户id
 */
@Data
public class UserIdRequest
        implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private Integer userId;
}
