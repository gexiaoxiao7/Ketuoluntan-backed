package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 信息id请求类
 */
@Data
public class SignIdRequest
        implements Serializable {
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 信息唯一标识
     */
    private Integer signId;
}
