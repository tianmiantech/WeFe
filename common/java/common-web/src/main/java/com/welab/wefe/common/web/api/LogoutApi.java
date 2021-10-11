/**
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

package com.welab.wefe.common.web.api;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiInput;
import com.welab.wefe.common.web.dto.NoneApiOutput;

/**
 * Log out
 *
 * @author zane.luo
 */
@Api(path = "logout", name = "退出登录")
public class LogoutApi extends AbstractApi<NoneApiInput, NoneApiOutput> {
    @Override
    protected ApiResult<NoneApiOutput> handle(NoneApiInput input) throws StatusCodeWithException {

        CurrentAccount.logout();
        return success();
    }
}
