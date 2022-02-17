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
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import com.welab.wefe.union.service.service.sms.SmsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author aaron.li
 * @date 2021/11/9 11:44
 **/
@Api(path = "sms/check_verification_code", name = "check sms verification code",  rsaVerify = true, login = false)
@FlowLimitByIp(count = 20, second = 10)
public class CheckVerificationCodeApi extends AbstractApi<CheckVerificationCodeApi.Input, NoneApiOutput> {
    @Autowired
    private SmsService smsService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        smsService.checkVerificationCodeValid(input.mobile, input.code, input.smsBusinessType);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "Mobile phone number check verification code", require = true)
        private String mobile;

        @Check(name = "verification code", require = true)
        private String code;

        @Check(name = "business type", require = true)
        private SmsBusinessType smsBusinessType;

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public SmsBusinessType getSmsBusinessType() {
            return smsBusinessType;
        }

        public void setSmsBusinessType(SmsBusinessType smsBusinessType) {
            this.smsBusinessType = smsBusinessType;
        }
    }
}
