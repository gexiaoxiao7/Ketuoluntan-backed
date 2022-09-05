package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.able.Checkable;
import com.suibe.suibe_mma.domain.able.NotSetNullable;
import com.suibe.suibe_mma.enumeration.MessageEE;
import com.suibe.suibe_mma.exception.MessageException;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.suibe.suibe_mma.util.DomainUtil.notSetNullHelp;

@Data
@TableName("mma_message")
public class Message
        implements Serializable, Checkable<Message, Integer>, NotSetNullable<Message> {

    /**
     * 信息唯一标识
     */
    @TableId(
            value = "messageId",
            type = IdType.AUTO
    )
    private Integer messageId;

    /**
     * 信息内容
     */
    @TableField("messageContent")
    private String messageContent;

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
     * 发送者删除
     */
    @TableField("sendDelete")
    private Boolean sendDelete;

    /**
     * 接收者删除
     */
    @TableField("receiveDelete")
    private Boolean receiveDelete;

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
     * 是否删除标志
     */
    @TableLogic
    @TableField("isDelete")
    private Boolean isDelete;

    /**
     * 序列化id
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public Message checkPrimaryKey(Integer id, IService<Message> service) throws MessageException {
        if (id == null) {
            MessageEE.MESSAGE_ID_NULL.throwE();
        }
        Message message = service.getById(id);
        if (message == null) {
            MessageEE.MESSAGE_ID_WRONG.throwE();
        }
        return message;
    }

    @Override
    public List<Message> checkPrimaryKey(List<Integer> ids, IService<Message> service) throws MessageException {
        if (ids == null) {
            MessageEE.MESSAGE_IDS_NULL.throwE();
        }
        List<Message> messages = service.list();
        if (messages.isEmpty()) {
            MessageEE.MESSAGE_IDS_WRONG.throwE();
        }
        return messages;
    }

    @Override
    public Message notSetNull(String column) throws IllegalAccessException {
        return notSetNullHelp(this, column, "messageId");
    }

    @Override
    public Message notSetNull(String[] columns) throws IllegalAccessException {
        return notSetNullHelp(this, columns, "messageId");
    }
}
