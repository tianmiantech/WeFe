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

package com.welab.wefe.board.service.api.account;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.AccountOutputModel;
import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "account/query", name = "query account by pagination")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<AccountOutputModel>> {

    @Autowired
    AccountService service;

    @Override
    protected ApiResult<PagingOutput<AccountOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(service.query(input));
    }

    public static class Input extends PagingInput {
        private String phoneNumber;

        private String nickname;
        private AuditStatus auditStatus;

        //region getter/setter

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public AuditStatus getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(AuditStatus auditStatus) {
            this.auditStatus = auditStatus;
        }

        //endregion
    }

}
