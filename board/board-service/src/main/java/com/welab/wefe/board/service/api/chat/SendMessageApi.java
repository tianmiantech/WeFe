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

package com.welab.wefe.board.service.api.chat;

import com.welab.wefe.board.service.constant.ChatConstant;
import com.welab.wefe.board.service.service.MemberChatService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Send messages through non WebSockets
 *
 * @author aaron.li
 **/
@Api(path = "chat/send_message", name = "Send messages through non WebSockets")
public class SendMessageApi extends AbstractApi<SendMessageApi.Input, SendMessageApi.Output> {
    @Autowired
    private MemberChatService memberChatService;


    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        JObject ret = memberChatService.sendMessage(input.toMemberId, input.toMemberName, input.toAccountId, input.toAccountName, input.content);
        String code = ret.getString(ChatConstant.KEY_CODE);
        String message = ret.getString(ChatConstant.KEY_MESSAGE);
        String memberChatId = ret.getString(ChatConstant.KEY_MEMBER_CHAT_ID);

        Output output = new Output();
        output.setMemberChatId(memberChatId);
        output.setMessageId(input.messageId);
        return "0".equals(code) ? success(output) : fail(StatusCode.SYSTEM_ERROR.getCode(), message, output);
    }

    public static class Input extends AbstractApiInput {
        /**
         * Receiver member ID
         */
        @Check(require = true)
        private String toMemberId;
        /**
         * Receiver member name
         */
        @Check(require = true)
        private String toMemberName;
        /**
         * Receiver account id
         */
        @Check(require = true)
        private String toAccountId;
        /**
         * Receiver account name
         */
        @Check(require = true)
        private String toAccountName;
        /**
         * message content
         */
        @Check(require = true)
        private String content;
        @Check(name = "Message ID used by the front end")
        private String messageId;

        public String getToMemberId() {
            return toMemberId;
        }

        public void setToMemberId(String toMemberId) {
            this.toMemberId = toMemberId;
        }

        public String getToMemberName() {
            return toMemberName;
        }

        public void setToMemberName(String toMemberName) {
            this.toMemberName = toMemberName;
        }

        public String getToAccountId() {
            return toAccountId;
        }

        public void setToAccountId(String toAccountId) {
            this.toAccountId = toAccountId;
        }

        public String getToAccountName() {
            return toAccountName;
        }

        public void setToAccountName(String toAccountName) {
            this.toAccountName = toAccountName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
    }

    public static class Output extends AbstractApiOutput {
        @Check(name = "Message ID used by the front end")
        private String messageId;
        @Check(name = "Back end database message ID")
        private String memberChatId;

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getMemberChatId() {
            return memberChatId;
        }

        public void setMemberChatId(String memberChatId) {
            this.memberChatId = memberChatId;
        }
    }
}
