package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 写信息请求类
 */
@Data
public class MessageWriteRequest
        implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 发送者id
     */
    private Integer sendId;

    /**
     * 信息内容
     */
    private String messageContent;

    /**
     * 接收者id
     */
    private Integer receiveId;
}
