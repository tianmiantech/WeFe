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

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.account.RegisterInput;
import com.welab.wefe.manager.service.mapper.AccountMapper;
import com.welab.wefe.manager.service.service.AccountService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
@Api(path = "account/register", name = "register", login = false)
public class RegisterApi extends AbstractApi<RegisterInput, AbstractApiOutput> {
    @Autowired
    private AccountService accountService;

    private AccountMapper mAccountMapper = Mappers.getMapper(AccountMapper.class);

    @Override
    protected ApiResult<AbstractApiOutput> handle(RegisterInput input) throws StatusCodeWithException, IOException {
        accountService.register(mAccountMapper.transfer(input));
        return success();   
    }
}
