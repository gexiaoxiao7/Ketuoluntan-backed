package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 回复类
 */
@Data
@TableName("mma_reply")
public class Reply implements Serializable {

    /**
     * 回复唯一标识
     */
    @TableId(value = "topicId", type = IdType.AUTO)
    private Long replyId;

    /**
     * 回复的题目id
     */
    @TableField("topicId")
    private Long topicId;

    /**
     * 回复者id
     */
    @TableField("userId")
    private Integer userId;

    /**
     * 回复内容
     */
    @TableField("replyContent")
    private String replyContent;

    /**
     * 回复点赞数
     */
    @TableField("replyLikes")
    private Integer replyLikes;

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
     * 题目是否删除标志
     */
    @TableLogic
    @TableField("isDelete")
    private Boolean isDelete;

    /**
     * 序列化id
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
