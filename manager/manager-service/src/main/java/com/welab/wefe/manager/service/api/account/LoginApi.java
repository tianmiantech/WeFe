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

import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.data.mongodb.repo.AccountMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.account.LoginInput;
import com.welab.wefe.manager.service.dto.account.LoginOutput;
import com.welab.wefe.manager.service.service.AccountService;
import com.welab.wefe.manager.service.util.ManagerSM4Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
@Api(path = "account/login", name = "login", login = false)
public class LoginApi extends AbstractApi<LoginInput, LoginOutput> {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountMongoRepo accountMongoRepo;

    @Override
    protected ApiResult<LoginOutput> handle(LoginInput input) throws StatusCodeWithException {
        String token = accountService.login(input.getPhoneNumber()
                , input.getPassword(), input.getKey(), input.getCode());
        Account account = accountMongoRepo.findByPhoneNumber(ManagerSM4Util.encryptPhoneNumber(input.getPhoneNumber()));
        if (null != account) {
            account.setPhoneNumber(ManagerSM4Util.decryptPhoneNumber(account.getPhoneNumber()));
        }
        LoginOutput output = new LoginOutput(token, account);
        return success(output);
    }
}
