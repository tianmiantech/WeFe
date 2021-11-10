package com.welab.wefe.union.service.service.sms;

import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.welab.wefe.common.util.JObject;

/**
 * @author aaron.li
 * @Date 2021/10/20
 **/
public class AliyunSmsResponse extends AbstractSmsResponse<SendSmsResponse> {
    private final static String RESP_STATUS_OK = "OK";

    public AliyunSmsResponse(SendSmsResponse data) {
        super(data);
    }

    @Override
    public String getReqId() {
        return data.getBody().getRequestId();
    }

    @Override
    public boolean success() {
        return RESP_STATUS_OK.equals(data.getBody().code);
    }

    @Override
    public String getRespBody() {
        return JObject.create(data.getBody()).toString();
    }

    @Override
    public String getMessage() {
        return data.getBody().getMessage();
    }
}
