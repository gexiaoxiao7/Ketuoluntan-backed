package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibe.suibe_mma.domain.able.Checkable;
import com.suibe.suibe_mma.enumeration.UserEE;
import com.suibe.suibe_mma.exception.UserException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户信息类
 */
@Data
@EqualsAndHashCode(exclude = {"score", "updateTime", "monthScore"})
@TableName("mma_user")
public class User
        implements Serializable, Checkable<User, Integer> {

    /**
     * 用户唯一标识
     */
    @TableId(
            value = "id",
            type = IdType.AUTO
    )
    private Integer id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户账户名
     */
    @TableField("userAccount")
    private String userAccount;

    /**
     * 用户密码
     */
    @TableField(
            value = "userPassword",
            select = false
    )
    private String userPassword;

    /**
     * 用户头像地址
     */
    @TableField("avatarUrl")
    private String avatarUrl;

    /**
     * 用户性别
     */
    private String gender;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户角色
     */
    @TableField("userRole")
    private Integer userRole;

    /**
     * 用户积分
     */
    private Integer score;

    /**
     * 用户月积分
     */
    @TableField("monthScore")
    private Integer monthScore;

    /**
     * 自我介绍
     */
    @TableField("selfIntroduction")
    private String selfIntroduction;

    /**
     * 用户是否删除标志
     */
    @TableLogic
    @TableField("isDelete")
    private Boolean isDelete;

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
     * 序列化id
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public User checkPrimaryKey(
            Integer id,
            IService<User> service) throws UserException {
        if (id == null) {
            UserEE.USER_ID_IS_NULL.throwE();
        }
        User user = service.getById(id);
        if (user == null) {
            UserEE.USER_ID_WRONG.throwE();
        }
        return user;
    }

    @Override
    public List<User> checkPrimaryKey(
            List<Integer> ids,
            IService<User> service) throws UserException {
        if (ids == null) {
            UserEE.USER_IDS_IS_NULL.throwE();
        }
        List<User> users = service.listByIds(ids);
        if (users.isEmpty()) {
            UserEE.USER_IDS_IS_WRONG.throwE();
        }
        return users;
    }
}
