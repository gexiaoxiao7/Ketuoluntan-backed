package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 2085298263565334420L;

    private String userAccount;

    private String userPassword;
}
