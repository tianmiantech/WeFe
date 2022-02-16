/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
