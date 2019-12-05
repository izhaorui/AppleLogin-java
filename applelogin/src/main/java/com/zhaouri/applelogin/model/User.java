package com.zhaouri.applelogin.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 功能描述
 *
 * @Author: zhaorui
 * @Date: 2019/1/3 13:36
 */
@Data
@ToString
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer uid;
    private String nickName;
}
