/*
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

package com.welab.wefe.board.service.api.account;


import com.welab.wefe.board.service.dto.globalconfig.AlertConfigModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.verification.code.common.CaptchaSendChannel;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiInput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Get verification code send channel
 */
@Api(path = "account/verification_code_send_channel", name = "Get verification code send channel", login = false)
public class VerificationCodeSendChannelApi extends AbstractApi<NoneApiInput, VerificationCodeSendChannelApi.Output> {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<VerificationCodeSendChannelApi.Output> handle(NoneApiInput input) throws Exception {
        AlertConfigModel alertConfigModel = globalConfigService.getModel(AlertConfigModel.class);
        return success(new Output(alertConfigModel.retrievePasswordCaptchaChannel));
    }

    public static class Output {
        public CaptchaSendChannel channel;

        public Output() {
        }

        public Output(CaptchaSendChannel channel) {
            this.channel = channel;
        }
    }
}
