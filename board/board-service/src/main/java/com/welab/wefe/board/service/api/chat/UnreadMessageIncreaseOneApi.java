/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import com.welab.wefe.board.service.database.entity.chat.MemberChatMySqlModel;
import com.welab.wefe.board.service.service.ChatUnreadMessageService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Unread message plus 1
 * <p>
 * Usage scenario: A and B are chatting (the active chat window of a is b), and suddenly C sends a message to A.
 * because the active window of a is B, for a, add 1 to the unread message of C.
 * at this time, the front end calls this interface to add 1 to the unread message of C
 * </p>
 *
 * @author aaron.li
 **/
@Api(path = "chat/unread_message_increase_one", name = "Unread message plus 1")
public class UnreadMessageIncreaseOneApi extends AbstractApi<UnreadMessageIncreaseOneApi.Input, NoneApiOutput> {

    @Autowired
    private ChatUnreadMessageService chatUnreadMessageService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException {
        chatUnreadMessageService.addChatUnreadMessage(buildModel(input));
        return success();
    }

    public static class Input extends AbstractApiInput {
        /**
         * Sender member id
         */
        @Check(require = true)
        private String fromMemberId;
        /**
         * Sender account id
         */
        @Check(require = true)
        private String fromAccountId;
        /**
         * Receiver member id
         */
        @Check(require = true)
        private String toMemberId;
        /**
         * Receiver account id
         */
        @Check(require = true)
        private String toAccountId;

        public String getFromMemberId() {
            return fromMemberId;
        }

        public void setFromMemberId(String fromMemberId) {
            this.fromMemberId = fromMemberId;
        }

        public String getFromAccountId() {
            return fromAccountId;
        }

        public void setFromAccountId(String fromAccountId) {
            this.fromAccountId = fromAccountId;
        }

        public String getToMemberId() {
            return toMemberId;
        }

        public void setToMemberId(String toMemberId) {
            this.toMemberId = toMemberId;
        }

        public String getToAccountId() {
            return toAccountId;
        }

        public void setToAccountId(String toAccountId) {
            this.toAccountId = toAccountId;
        }
    }


    private MemberChatMySqlModel buildModel(UnreadMessageIncreaseOneApi.Input input) {
        MemberChatMySqlModel model = new MemberChatMySqlModel();
        model.setFromMemberId(input.getFromMemberId());
        model.setFromAccountId(input.getFromAccountId());
        model.setToMemberId(input.getToMemberId());
        model.setToAccountId(input.getToAccountId());

        return model;
    }

}
