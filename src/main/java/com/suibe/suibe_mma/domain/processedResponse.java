package com.suibe.suibe_mma.domain;

import lombok.Data;

import java.io.Serializable;
@Data
public class processedResponse implements Serializable {

    private static final long serialVersionUID = -7657517096595016751L;

    private String main;

    private String attribute;

    private String value;

    private String message;

}
