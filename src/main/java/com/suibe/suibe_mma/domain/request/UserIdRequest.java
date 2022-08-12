package com.suibe.suibe_mma.domain.request;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserIdRequest implements Serializable {

    private static final long serialVersionUID = 1959131336428604513L;

    private Integer userId;
}
