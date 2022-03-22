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

package com.welab.wefe.manager.service.api.account;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.account.AccountEnableInput;
import com.welab.wefe.manager.service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
@Api(path = "account/enable", name = "change account enable status")
public class EnableApi extends AbstractApi<AccountEnableInput, AbstractApiOutput> {
    @Autowired
    private AccountService accountService;


    @Override
    protected ApiResult<AbstractApiOutput> handle(AccountEnableInput input) throws Exception {
        accountService.enableUser(input.getAccountId(),input.isEnable());
        return success();
    }
}
