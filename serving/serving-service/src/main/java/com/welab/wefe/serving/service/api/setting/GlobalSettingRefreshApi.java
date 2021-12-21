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

package com.welab.wefe.serving.service.api.setting;

import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.GlobalSettingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(
        path = "global_setting/refresh",
        name = "更新 全局设置",
        rsaVerify = true,
        login = false,
        domain = Caller.Board
)
public class GlobalSettingRefreshApi extends AbstractNoneOutputApi<GlobalSettingUpdateApi.Input> {

    @Autowired
    private GlobalSettingService globalSettingService;

    @Override
    protected ApiResult<?> handler(GlobalSettingUpdateApi.Input input) {
        globalSettingService.updateMemberInfo(input);
        return success();
    }
}
