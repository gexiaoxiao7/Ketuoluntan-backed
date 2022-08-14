package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.able.Likable;
import com.suibe.suibe_mma.enumeration.ReplyExceptionEnumeration;
import com.suibe.suibe_mma.service.ReplyService;
import com.suibe.suibe_mma.service.UserService;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.io.Serializable;
import java.util.Date;

import static com.suibe.suibe_mma.util.ServiceUtil.likeHelper;

/**
 * 回复类
 */
@Data
@TableName("mma_reply")
public class Reply implements Serializable, Likable {

    /**
     * 回复唯一标识
     */
    @TableId(value = "replyId", type = IdType.AUTO)
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

    @Override
    public Likable like(
            Integer userId,
            Integer flag,
            @NotNull RedisTemplate<String, Object> template,
            String key,
            IService<? extends Likable> service,
            UserService userService) {
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
        ReplyService replyService = (ReplyService) service;
        if (!replyService.updateById(this)) {
            ReplyExceptionEnumeration.REPLY_LIKE_UPDATE_FAILED.throwReplyException();
        }
        userService.update(likeHelper(rflag, this.userId));
        return replyService.getById(replyId);
    }
}
