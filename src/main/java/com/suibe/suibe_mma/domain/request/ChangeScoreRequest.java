package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 改变积分类
 */
@Data
public class ChangeScoreRequest
        implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 积分改变值
     */
    private Integer score;

    /**
     * 用户唯一标识
     */
    private Integer id;
}
