package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 写回复相关配置信息类
 */
@Data
public class ReplyWriteRequest
        implements Serializable {

    /**
     * 回复的题目id
     */
    private Long topicId;

    /**
     * 回复者id
     */
    private Integer userId;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;
}
