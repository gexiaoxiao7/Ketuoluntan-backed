package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 举报信息请求类
 */
@Data
public class SignReportRequest
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
     * 报名者的姓名
     */
    private String signName;

    /**
     * 报名者的学号
     */
    private String signSId;

    /**
     * 报名者qq号
     */
    private String qqId;

    /**
     * 报名者的专业班级
     */
    private String majorClass;

    /**
     * 报名者的电话号码
     */
    private String phoneNumber;

    /**
     * 报名者的电子邮件
     */
    private String email;

    /**
     * 报名者的相关经历
     */
    private String experience;

    /**
     * 报名者加入的原因
     */
    private String reasons;

}
