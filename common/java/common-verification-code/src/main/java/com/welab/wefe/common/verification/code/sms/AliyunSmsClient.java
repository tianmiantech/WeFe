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

package com.welab.wefe.common.verification.code.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.verification.code.AbstractResponse;

import java.util.HashMap;
import java.util.Map;

public class AliyunSmsClient extends AbstractClient {
    public final static String ACCESS_KEY_ID = "accessKeyId";
    public final static String ACCESS_KEY_SECRET = "accessKeySecret";
    public final static String SIGN_NAME = "signName";
    public final static String TEMPLATE_CODE = "templateCode";

    public AliyunSmsClient(Map<String, Object> extendParams) {
        super(extendParams);
    }

    @Override
    public AbstractResponse send(String mobile, String verificationCode) throws Exception {
        JObject extendParams = JObject.create(getExtendParams());
        Config config = new Config().setAccessKeyId(extendParams.getString(ACCESS_KEY_ID))
                .setAccessKeySecret(extendParams.getString(ACCESS_KEY_SECRET));
        config.endpoint = "dysmsapi.aliyuncs.com";
        Client client = new Client(config);
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setPhoneNumbers(mobile);
        sendSmsRequest.setSignName(extendParams.getString(SIGN_NAME));
        sendSmsRequest.setTemplateCode(extendParams.getString(TEMPLATE_CODE));
        sendSmsRequest.setTemplateParam(JObject.create("code", verificationCode).toString());
        SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
        return new AliyunSmsResponse(sendSmsResponse);
    }

    public static Map<String, Object> buildExtendParams(String accessKeyId, String accessKeySecret, String signName, String templateCode) {
        Map<String, Object> extendParams = new HashMap<>(16);
        extendParams.put(ACCESS_KEY_ID, accessKeyId);
        extendParams.put(ACCESS_KEY_SECRET, accessKeySecret);
        extendParams.put(SIGN_NAME, signName);
        extendParams.put(TEMPLATE_CODE, templateCode);
        return extendParams;
    }
}
