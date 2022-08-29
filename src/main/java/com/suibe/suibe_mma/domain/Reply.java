package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.able.Checkable;
import com.suibe.suibe_mma.domain.able.Likable;
import com.suibe.suibe_mma.domain.able.SetNullable;
import com.suibe.suibe_mma.enumeration.ReplyEE;
import com.suibe.suibe_mma.exception.ReplyException;
import com.suibe.suibe_mma.service.UserService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.suibe.suibe_mma.util.DomainUtil.notSetNullHelp;
import static com.suibe.suibe_mma.util.ServiceUtil.likeHelper;

/**
 * 回复类
 */
@Data
@EqualsAndHashCode(exclude = {"replyLikes", "updateTime"})
@TableName("mma_reply")
public class Reply
        implements Serializable, Likable<Reply>, Checkable<Reply, Long>, SetNullable<Reply> {

    /**
     * 回复唯一标识
     */
    @TableId(
            value = "replyId",
            type = IdType.AUTO
    )
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

    @Override
    public Reply like(
            Integer userId,
            Integer flag,
            @NotNull RedisTemplate<String, Object> template,
            String key,
            IService<Reply> service,
            UserService userService) throws ReplyException {
        boolean rflag = false;
        SetOperations<String, Object> operations = template.opsForSet();
        if (flag == -1) {
            operations.add(key, userId);
            replyLikes += 1;
            rflag = true;
        } else {
            operations.remove(key, userId);
            replyLikes -= 1;
        }
        updateTime = null;
        if (!service.updateById(this)) {
            ReplyEE.REPLY_LIKE_UPDATE_FAILED.throwE();
        }
        userService.update(likeHelper(rflag, this.userId));
        return service.getById(replyId);
    }

    @Override
    public Reply checkPrimaryKey(
            Long id,
            IService<Reply> service) throws ReplyException {
        if (id == null) {
            ReplyEE.REPLY_ID_IS_NULL.throwE();
        }
        Reply reply = service.getById(id);
        if (reply == null) {
            ReplyEE.REPLY_ID_IS_WRONG.throwE();
        }
        return reply;
    }

    @Override
    public List<Reply> checkPrimaryKey(
            List<Long> ids,
            IService<Reply> service) throws ReplyException {
        if (ids == null) {
            ReplyEE.REPLY_IDS_IS_NULL.throwE();
        }
        List<Reply> replies = service.listByIds(ids);
        if (replies.isEmpty()) {
            ReplyEE.REPLY_IDS_IS_WRONG.throwE();
        }
        return replies;
    }

    @Override
    public Reply notSetNull(String column) throws IllegalAccessException {
        return notSetNullHelp(this, column, "replyId");
    }

    @Override
    public Reply notSetNull(String[] columns) throws IllegalAccessException {
        return notSetNullHelp(this, columns, "replyId");
    }
}
