package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目信息类
 */
@Data
@EqualsAndHashCode(exclude = {"topicLikes"})
@TableName("mma_topic")
public class Topic implements Serializable {

    /**
     * 题目唯一标识
     */
    @TableId(value = "topicId", type = IdType.AUTO)
    private Long topicId;

    /**
     * 题目标题
     */
    @TableField("topicTitle")
    private String topicTitle;

    /**
     * 题目内容
     */
    @TableField("topicContent")
    private String topicContent;

    /**
     * 题目点赞数
     */
    @TableField("topicLikes")
    private Integer topicLikes;

    /**
     * 题目是否被选为精选
     */
    @TableField("isStared")
    private String isStared;

    /**
     * 创建时间
     */
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 出题人唯一标识
     */
    @TableField("userId")
    private Integer userId;

    /**
     * 题目是否删除标志
     */
    @TableLogic
    @TableField("isDelete")
    private Boolean isDelete;

    /**
     * 回复数
     */
    @TableField("replyNum")
    private Long replyNum;

    /**
     * 序列化id
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
