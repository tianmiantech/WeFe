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

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.account.QueryAccountInput;
import com.welab.wefe.manager.service.dto.account.QueryAccountOutput;
import com.welab.wefe.manager.service.mapper.AccountMapper;
import com.welab.wefe.manager.service.service.AccountService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/5
 */
@Api(path = "account/query", name = "query account by pagination")
public class QueryApi extends AbstractApi<QueryAccountInput, PageOutput<QueryAccountOutput>> {
    @Autowired
    private AccountService accountService;
    private AccountMapper mAccountMapper = Mappers.getMapper(AccountMapper.class);

    @Override
    protected ApiResult<PageOutput<QueryAccountOutput>> handle(QueryAccountInput input) throws StatusCodeWithException, IOException {
        PageOutput<Account> pageOutput = accountService.findList(input);
        List<QueryAccountOutput> list = pageOutput.getList().stream()
                .map(mAccountMapper::transferAccountToQueryUserOutput)
                .collect(Collectors.toList());

        return success(new PageOutput<>(
                pageOutput.getPageIndex(),
                pageOutput.getTotal(),
                pageOutput.getPageSize(),
                pageOutput.getTotalPage(),
                list
        ));
    }
}
