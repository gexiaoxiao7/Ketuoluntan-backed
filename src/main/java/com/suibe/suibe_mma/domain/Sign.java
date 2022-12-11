package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.able.Checkable;
import com.suibe.suibe_mma.domain.able.Likable;
import com.suibe.suibe_mma.domain.able.NotSetNullable;
import com.suibe.suibe_mma.enumeration.ReplyEE;
import com.suibe.suibe_mma.exception.ReplyException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.suibe.suibe_mma.util.DomainUtil.notSetNullHelp;

/**
 * 回复类
 */
@Data
@EqualsAndHashCode(exclude = {"updateTime"})
@TableName("mma_sign")
public class Sign
        implements Serializable, Checkable<Sign, Integer>, NotSetNullable<Sign> {

    /**
     * 报名唯一标识
     */
    @TableId(
            value = "signId",
            type = IdType.AUTO
    )
    private Integer signId;

    /**
     * 姓名
     */
    @TableField("signName")
    private String signName;

    /**
     * 学号
     */
    @TableField("signSId")
    private String signSId;

    /**
     * QQ号
     */
    @TableField("qqId")
    private String qqId;

    /**
     * 专业班级
     */
    @TableField("majorClass")
    private String majorClass;

    /**
     * 电话号码
     */
    @TableField("phoneNumber")
    private String phoneNumber;

    /**
     * 电子邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 数学相关经历
     */
    @TableField("experience")
    private String experience;

    /**
     * 加入的原因
     */
    @TableField("reasons")
    private String reasons;

    /**
     * 创建时间
     */
    @TableField(
            value = "createTime",
            fill = FieldFill.INSERT
    )
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(
            value = "updateTime",
            fill = FieldFill.INSERT_UPDATE
    )
    private Date updateTime;

    /**
     * 发送者唯一标识
     */
    @TableField("sendId")
    private Integer sendId;

    /**
     * 接收者唯一标识
     */
    @TableField("receiveId")
    private Integer receiveId;

    /**
     * 是否读
     */
    @TableField("isRead")
    private Boolean isRead;

    /**
     * 题目是否删除标志
     */
    @TableLogic
    @TableField("isDelete")
    private Boolean isDelete;

    /**
     * 接收者删除
     */
    @TableField("receiveDelete")
    private Boolean receiveDelete;

    /**
     * 序列化id
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public Sign checkPrimaryKey(
            Integer id,
            IService<Sign> service) throws ReplyException {
        if (id == null) {
            ReplyEE.REPLY_ID_IS_NULL.throwE();
        }
        Sign reply = service.getById(id);
        if (reply == null) {
            ReplyEE.REPLY_ID_IS_WRONG.throwE();
        }
        return reply;
    }

    @Override
    public List<Sign> checkPrimaryKey(
            List<Integer> ids,
            IService<Sign> service) throws ReplyException {
        if (ids == null) {
            ReplyEE.REPLY_IDS_IS_NULL.throwE();
        }
        List<Sign> replies = service.listByIds(ids);
        if (replies.isEmpty()) {
            ReplyEE.REPLY_IDS_IS_WRONG.throwE();
        }
        return replies;
    }

    @Override
    public Sign notSetNull(String column) throws IllegalAccessException {
        return notSetNullHelp(this, column, "replyId");
    }

    @Override
    public Sign notSetNull(String[] columns) throws IllegalAccessException {
        return notSetNullHelp(this, columns, "replyId");
    }
}
