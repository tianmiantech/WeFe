/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.union;

import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.enums.SmsBusinessType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author aaron.li
 * @date 2021/11/11 09:45
 **/
@Api(path = "union/send_forget_password_sms_code", name = "send sms verification code", login = false)
public class SendForgetPasswordSmsCodeApi extends AbstractApi<SendForgetPasswordSmsCodeApi.Input, NoneApiOutput> {
    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        unionService.sendVerificationCode(input.phoneNumber, SmsBusinessType.AccountForgetPasswordVerificationCode);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        private String phoneNumber;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}
