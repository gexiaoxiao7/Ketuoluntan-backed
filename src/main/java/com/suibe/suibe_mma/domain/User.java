package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息类
 */
@Data
@EqualsAndHashCode(exclude = {"userPassword", "isDelete", "updateTime"})
@TableName("mma_user")
public class User implements Serializable {

    /**
     * 用户唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
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
    @TableField("userPassword")
    private String userPassword;

    /**
     * 用户头像地址
     */
    @TableField("avatarUrl")
    private String avatarUrl;

    /**
     * 用户性别
     */
    private Boolean gender;

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
     * 用户是否删除标志
     */
    @TableLogic
    @TableField("isDelete")
    private Boolean isDelete;

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
     * 序列化id
     */
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
