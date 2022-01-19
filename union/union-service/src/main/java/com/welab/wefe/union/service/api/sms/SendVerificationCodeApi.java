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

package com.welab.wefe.union.service.api.sms;

import com.welab.wefe.common.data.mongodb.constant.SmsBusinessType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.FlowLimitByIp;
import com.welab.wefe.common.web.api.base.FlowLimitByMobile;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.service.sms.SmsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * send sms verification code
 *
 * @author aaron.li
 * @Date 2021/10/19
 **/
@Api(path = "sms/send_verification_code", name = "send sms verification code",  rsaVerify = true, login = false)
@FlowLimitByIp(count = 50, second = 24 * 60 * 60)
@FlowLimitByMobile(count = 10, second = 24 * 60 * 60)
public class SendVerificationCodeApi extends AbstractApi<SendVerificationCodeApi.Input, SendVerificationCodeApi.Output> {

    @Autowired
    private SmsService smsService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        smsService.sendVerificationCode(input.mobile, input.smsBusinessType);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "Mobile phone number receiving verification code", require = true)
        private String mobile;

        @Check(name = "business type", require = true)
        private SmsBusinessType smsBusinessType;

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public SmsBusinessType getSmsBusinessType() {
            return smsBusinessType;
        }

        public void setSmsBusinessType(SmsBusinessType smsBusinessType) {
            this.smsBusinessType = smsBusinessType;
        }
    }

    public static class Output {

        private String verificationCode;

        public Output(String verificationCode) {
            this.verificationCode = verificationCode;
        }

        public String getVerificationCode() {
            return verificationCode;
        }

        public void setVerificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
        }
    }
}
