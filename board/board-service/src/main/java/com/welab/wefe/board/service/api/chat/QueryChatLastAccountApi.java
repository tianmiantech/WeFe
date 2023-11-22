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

import com.welab.wefe.board.service.dto.entity.ChatLastAccountOutputModel;
import com.welab.wefe.board.service.service.ChatLastAccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Query recent chat account
 *
 * @author aaron.li
 **/
@Api(path = "chat/chat_last_account", name = "Query recent chat account")
public class QueryChatLastAccountApi extends AbstractApi<QueryChatLastAccountApi.Input, QueryChatLastAccountApi.Output> {

    @Autowired
    private ChatLastAccountService chatLastAccountService;

    @Override
    protected ApiResult<QueryChatLastAccountApi.Output> handle(Input input) throws StatusCodeWithException {
        return success(new Output().setList(chatLastAccountService.query(input.accountId)));
    }

    public static class Input extends AbstractApiInput {
        /**
         * account id
         */
        @Check(require = true)
        private String accountId;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }
    }

    public static class Output extends AbstractApiOutput {
        private List<ChatLastAccountOutputModel> list;

        public List<ChatLastAccountOutputModel> getList() {
            return list;
        }

        public Output setList(List<ChatLastAccountOutputModel> list) {
            this.list = list;
            return this;
        }
    }

}
