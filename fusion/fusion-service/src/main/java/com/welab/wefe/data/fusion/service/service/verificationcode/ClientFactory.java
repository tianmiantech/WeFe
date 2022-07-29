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

package com.welab.wefe.data.fusion.service.service.verificationcode;

import com.welab.wefe.common.verification.code.AbstractClient;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.VerificationCodeBusinessType;
import com.welab.wefe.common.wefe.enums.VerificationCodeSendChannel;
import com.welab.wefe.data.fusion.service.config.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for sending verification code client
 *
 * @author aaron.li
 * @date 2022/1/19 14:05
 **/
public class ClientFactory {

    /**
     * Get sending verification code client
     *
     * @param sendChannel  Send channel
     * @param businessType verification code business type
     * @return Sending verification code client
     * @throws Exception
     */
    public static AbstractClient getClient(VerificationCodeSendChannel sendChannel, VerificationCodeBusinessType businessType) throws Exception {
        switch (sendChannel) {
            case sms:
                return buildAliyunSmsClient(businessType);
            case email:
//                return buildEmailClient(businessType);
            default:
        }

        return null;
    }

    /**
     * Build Aliyun sms sending verification code client
     */
    private static AbstractClient buildAliyunSmsClient(VerificationCodeBusinessType businessType) throws Exception {
        Config config = Launcher.CONTEXT.getBean(Config.class);
        String templateCode = "";
        switch (businessType) {
            case accountForgetPassword:
                templateCode = config.getSmsAliyunAccountForgetPasswordVerificationCodeTemplateCode();
                break;
            case memberRegister:
                templateCode = config.getSmsAliyunMemberregisterVerificationCodeTemplateCode();
                break;
            default:
        }

        Map<String, Object> smsRequest = new HashMap<>(16);
        smsRequest.put("SignName", config.getSmsAliyunSignName());
        smsRequest.put("templateCode", templateCode);

        String aliyunAccessKeyId = config.getSmsAccessKeyId();
        String accessKeySecret = config.getSmsAccessKeySecret();
        return AliyunSmsClient.createClient(aliyunAccessKeyId, accessKeySecret, smsRequest);
    }
}
