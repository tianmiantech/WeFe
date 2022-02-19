/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.service.verificationcode;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.verification.code.AbstractResponse;

import java.util.Map;

/**
 * Aliyun sms client
 *
 * @author aaron.li
 * @date 2022/1/19 10:48
 **/
public class AliyunSmsClient extends AbstractClient {
    private Client client;

    public AliyunSmsClient(Map<String, Object> extendParams) {
        super(extendParams);
    }

    public static AliyunSmsClient createClient(String accessKeyId, String accessKeySecret, Map<String, Object> extendParams) throws Exception {
        Config config = new Config().setAccessKeyId(accessKeyId).setAccessKeySecret(accessKeySecret);
        config.endpoint = "dysmsapi.aliyuncs.com";
        AliyunSmsClient aliyunSmsClient = new AliyunSmsClient(extendParams);
        aliyunSmsClient.client = new Client(config);
        return aliyunSmsClient;
    }

    @Override
    public AbstractResponse send(String mobile, String verificationCode) throws Exception {
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setPhoneNumbers(mobile);
        Map<String, Object> extendParams = getExtendParams();
        sendSmsRequest.setSignName(String.valueOf(extendParams.get("SignName")));
        sendSmsRequest.setTemplateCode(String.valueOf(extendParams.get("templateCode")));
        sendSmsRequest.setTemplateParam(JObject.create("code", verificationCode).toString());
        SendSmsResponse sendSmsResponse = this.client.sendSms(sendSmsRequest);
        return new AliyunSmsResponse(sendSmsResponse);
    }
}
