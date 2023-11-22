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

import com.welab.wefe.board.service.service.ChatLastAccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Add a recent chat account
 * <p>
 * Scenario: when the front end clicks an account from the account list, it calls the interface to add the account to the database
 * </p>
 *
 * @author aaron.li
 **/
@Api(path = "chat/add_chat_last_account", name = "Add a recent chat account")
public class AddChatLastAccountApi extends AbstractApi<AddChatLastAccountApi.Input, NoneApiOutput> {

    @Autowired
    private ChatLastAccountService chatLastAccountService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException {
        chatLastAccountService.add(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        /**
         * account id
         */
        @Check(require = true)
        private String accountId;
        /**
         * account name
         */
        @Check(require = true)
        private String accountName;
        /**
         * member id
         */
        @Check(require = true)
        private String memberId;
        /**
         * member name
         */
        @Check(require = true)
        private String memberName;
        /**
         * liaison account id
         */
        @Check(require = true)
        private String liaisonAccountId;
        /**
         * liaison account name
         */
        @Check(require = true)
        private String liaisonAccountName;
        /**
         * liaison member id
         */
        @Check(require = true)
        private String liaisonMemberId;
        /**
         * liaison member name
         */
        @Check(require = true)
        private String liaisonMemberName;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getLiaisonAccountId() {
            return liaisonAccountId;
        }

        public void setLiaisonAccountId(String liaisonAccountId) {
            this.liaisonAccountId = liaisonAccountId;
        }

        public String getLiaisonAccountName() {
            return liaisonAccountName;
        }

        public void setLiaisonAccountName(String liaisonAccountName) {
            this.liaisonAccountName = liaisonAccountName;
        }

        public String getLiaisonMemberId() {
            return liaisonMemberId;
        }

        public void setLiaisonMemberId(String liaisonMemberId) {
            this.liaisonMemberId = liaisonMemberId;
        }

        public String getLiaisonMemberName() {
            return liaisonMemberName;
        }

        public void setLiaisonMemberName(String liaisonMemberName) {
            this.liaisonMemberName = liaisonMemberName;
        }
    }
}
