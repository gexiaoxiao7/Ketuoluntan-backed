package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 传递回复id
 */
@Data
public class ReplyIdRequest
        implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 回复唯一标识
     */
    private Long replyId;
}
