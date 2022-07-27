package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -6711190183287286640L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
