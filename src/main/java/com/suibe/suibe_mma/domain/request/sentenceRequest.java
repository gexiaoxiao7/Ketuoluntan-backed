package com.suibe.suibe_mma.domain.request;


import lombok.Data;

import java.io.Serializable;

/*
 * 分词原始句子请求类
 */
@Data
public class sentenceRequest implements Serializable {



    private static final long serialVersionUID = 9150232323977954596L;


    private String sentenceContent;

}
