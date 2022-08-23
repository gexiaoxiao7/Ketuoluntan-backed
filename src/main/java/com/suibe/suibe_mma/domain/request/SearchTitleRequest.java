package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询题目相关请求类
 */
@Data
public class SearchTitleRequest
        implements Serializable {

    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * 查询题目标题
     */
    private String searchTitle;
}
