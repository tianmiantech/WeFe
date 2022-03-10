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

package com.welab.wefe.board.service.api.account;

import com.welab.wefe.board.service.dto.vo.OnlineAccountOutput;
import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Query the online account of the exchange center
 *
 * @author aaron.li
 **/
@Api(path = "account/query_online", name = "Query the online account of the exchange center")
public class QueryOnlineApi extends AbstractApi<QueryOnlineApi.Input, QueryOnlineApi.Output> {
    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<QueryOnlineApi.Output> handle(QueryOnlineApi.Input input) throws StatusCodeWithException {
        Output output = new Output();
        output.setList(accountService.queryOnlineAccount(input));
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        /**
         * member id
         */
        @Check(require = true)
        private String memberId;
        @Check(name = "Account ID (if it is not empty, it means that you specify to query the online status of the account)")
        private String accountId;

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }
    }


    public static class Output {

        private List<OnlineAccountOutput> list;

        public List<OnlineAccountOutput> getList() {
            return list;
        }

        public void setList(List<OnlineAccountOutput> list) {
            this.list = list;
        }
    }

}
