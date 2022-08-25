package com.suibe.suibe_mma.domain.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户id与积分
 */
@Data
@AllArgsConstructor
public class UserIdAndScore
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
     * 用户总积分
     */
    private Integer score;
}
