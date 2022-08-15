package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目上传请求类
 */
@Data
public class TopicUploadRequest
        implements Serializable {
    /**
     * 题目标题
     */
    private String topicTitle;

    /**
     * 题目内容
     */
    private String topicContent;

    /**
     * 上传者唯一标识
     */
    private Integer userId;

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;
}
