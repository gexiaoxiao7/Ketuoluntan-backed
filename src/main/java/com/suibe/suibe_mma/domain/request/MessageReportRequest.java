package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 举报信息请求类
 */
@Data
public class MessageReportRequest
        implements Serializable {
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 发送者唯一标识
     */
    private Integer sendId;

    /**
     * 信息内容
     */
    private String messageContent;
}
