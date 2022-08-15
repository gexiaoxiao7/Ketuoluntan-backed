package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.able.Likable;
import com.suibe.suibe_mma.enumeration.TopicExceptionEnumeration;
import com.suibe.suibe_mma.exception.TopicException;
import com.suibe.suibe_mma.service.TopicService;
import com.suibe.suibe_mma.service.UserService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.io.Serializable;
import java.util.Date;

import static com.suibe.suibe_mma.util.ServiceUtil.likeHelper;

/**
 * 题目信息类
 */
@Data
@EqualsAndHashCode(exclude = {"topicLikes"})
@TableName("mma_topic")
public class Topic implements Serializable, Likable<Topic> {

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

    @Override
    public Topic like(
            Integer userId,
            Integer flag,
            @NotNull RedisTemplate<String, Object> template,
            String key,
            IService<Topic> service,
            UserService userService) throws TopicException {
        boolean tflag = false;
        SetOperations<String, Object> operations = template.opsForSet();
        if (flag == -1) {
            operations.add(key, userId);
            topicLikes += 1;
            tflag = true;
        } else {
            operations.remove(key, userId);
            topicLikes -= 1;
        }
        updateTime = null;
        if (!service.updateById(this)) {
            TopicExceptionEnumeration.TOPIC_LIKE_UPDATE_FAILED.throwTopicException();
        }
        userService.update(likeHelper(tflag, this.userId));
        return service.getById(topicId);
    }
}
