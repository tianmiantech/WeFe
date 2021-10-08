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
 * Delete recent chat account
 *
 * @author aaron.li
 **/
@Api(path = "chat/delete_chat_last_account", name = "Delete recent chat account")
public class DeleteChatLastAccountApi extends AbstractApi<DeleteChatLastAccountApi.Input, NoneApiOutput> {

    @Autowired
    private ChatLastAccountService chatLastAccountService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException {
        chatLastAccountService.delete(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        /**
         * account id
         */
        @Check(require = true)
        private String accountId;
        /**
         * liaison account id
         */
        @Check(require = true)
        private String liaisonAccountId;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getLiaisonAccountId() {
            return liaisonAccountId;
        }

        public void setLiaisonAccountId(String liaisonAccountId) {
            this.liaisonAccountId = liaisonAccountId;
        }
    }
}
