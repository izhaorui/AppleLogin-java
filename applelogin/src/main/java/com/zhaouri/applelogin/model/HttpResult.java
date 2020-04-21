package com.zhaouri.applelogin.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @JsonInclude(JsonInclude.Include.NON_NULL)表示,如果值为null,则不返回该字段
 * @Author: zhaorui
 * @Date: 2018/9/7 22:42
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpResult {
    private Integer code;
    private String msg;
    private Object data;

    public HttpResult() {
    }

    public HttpResult(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
