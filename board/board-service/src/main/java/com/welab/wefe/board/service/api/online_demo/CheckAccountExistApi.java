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

package com.welab.wefe.board.service.api.online_demo;

import com.welab.wefe.board.service.base.OnlineDemoApi;
import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@OnlineDemoApi
@Api(path = "account/online_demo/exist", name = "check if account already exists", login = false)
public class CheckAccountExistApi extends AbstractApi<CheckAccountExistApi.Input, CheckAccountExistApi.Output> {
    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        boolean exist = accountService.exist(input.phoneNumber);
        return success(new Output(exist));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "手机号")
        private String phoneNumber;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class Output {
        private boolean exist;

        public Output(boolean exist) {
            this.exist = exist;
        }

        public boolean isExist() {
            return exist;
        }

        public void setExist(boolean exist) {
            this.exist = exist;
        }
    }
}
