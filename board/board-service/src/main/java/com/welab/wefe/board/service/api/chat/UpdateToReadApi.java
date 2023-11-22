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

package com.welab.wefe.board.service.api.chat;

import com.welab.wefe.board.service.service.MemberChatService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Update the chat message of the account as read
 *
 * @author Johnny.lin
 */
@Api(path = "chat/update_to_read", name = "Update the chat message of the account as read")
public class UpdateToReadApi extends AbstractNoneOutputApi<UpdateToReadApi.Input> {

    @Autowired
    MemberChatService memberChatService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        memberChatService.messageUpdateToRead(input);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(require = true)
        private String fromAccountId;

        @Check(require = true)
        private String toAccountId;


        public String getFromAccountId() {
            return fromAccountId;
        }

        public void setFromAccountId(String fromAccountId) {
            this.fromAccountId = fromAccountId;
        }

        public String getToAccountId() {
            return toAccountId;
        }

        public void setToAccountId(String toAccountId) {
            this.toAccountId = toAccountId;
        }
    }
}
