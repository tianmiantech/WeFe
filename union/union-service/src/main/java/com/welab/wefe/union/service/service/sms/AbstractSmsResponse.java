package com.welab.wefe.union.service.service.sms;

import java.util.UUID;

/**
 * @author aaron.li
 * @Date 2021/10/20
 **/
public abstract class AbstractSmsResponse<T> {

    public T data;

    public AbstractSmsResponse(T data) {
        this.data = data;
    }

    public String getReqId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Is send success
     *
     * @return true or false
     */
    public abstract boolean success();

    /**
     * Get response content
     *
     * @return Get response body
     */
    public abstract String getRespBody();

    public String getMessage() {
        return null;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
