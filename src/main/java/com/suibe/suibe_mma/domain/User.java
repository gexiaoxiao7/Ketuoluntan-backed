package com.suibe.suibe_mma.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("mma_user")
public class User implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String username;
    @TableField("userAccount")
    private String userAccount;
    @TableField("userPassword")
    private String userPassword;
    @TableField("avatarUrl")
    private String avatarUrl;
    private Boolean gender;
    private String email;
    @TableField("userRole")
    private Integer userRole;
    private Integer score;
    @TableLogic
    @TableField("isDelete")
    private Boolean isDelete;
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
