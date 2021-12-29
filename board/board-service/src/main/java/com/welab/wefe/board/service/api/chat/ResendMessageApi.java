/*
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

package com.welab.wefe.board.service.api.chat;

import com.welab.wefe.board.service.service.MemberChatService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Resend send failure message
 *
 * @author aaron.li
 **/
@Api(path = "chat/resend_message", name = "Resend send failure message")
public class ResendMessageApi extends AbstractApi<ResendMessageApi.Input, NoneApiOutput> {

    @Autowired
    private MemberChatService memberChatService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException {
        memberChatService.resendMessage(input.memberChatId);
        return success();
    }

    public static class Input extends AbstractApiInput {
        /**
         * Back end database message ID
         */
        @Check(require = true)
        private String memberChatId;

        public String getMemberChatId() {
            return memberChatId;
        }

        public void setMemberChatId(String memberChatId) {
            this.memberChatId = memberChatId;
        }
    }
}
