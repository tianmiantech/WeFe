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
import com.welab.wefe.board.service.dto.vo.AccountInputModel;
import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.BoardUserSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zan.luo
 */
@OnlineDemoApi
@Api(path = "account/online_demo/create", name = "open an experience account", login = false)
public class CreateOnlineDemoAccountApi extends AbstractNoneOutputApi<AccountInputModel> {
    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<?> handler(AccountInputModel input) throws StatusCodeWithException {
        accountService.register(input, BoardUserSource.online_demo);
        return success();
    }

}
