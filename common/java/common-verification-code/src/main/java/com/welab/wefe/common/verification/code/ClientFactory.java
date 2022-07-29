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

package com.welab.wefe.common.verification.code;

import com.welab.wefe.common.verification.code.common.VerificationCodeSendChannel;
import com.welab.wefe.common.verification.code.email.EmailClient;
import com.welab.wefe.common.verification.code.sms.AliyunSmsClient;

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
     * @param extendParams extendParams
     * @return Sending verification code client
     * @throws Exception
     */
    public static AbstractClient getClient(VerificationCodeSendChannel sendChannel, Map<String, Object> extendParams) throws Exception {
        switch (sendChannel) {
            case sms:
                return new AliyunSmsClient(extendParams);
            case email:
                return new EmailClient(extendParams);
            default:
        }
        return null;
    }

}
