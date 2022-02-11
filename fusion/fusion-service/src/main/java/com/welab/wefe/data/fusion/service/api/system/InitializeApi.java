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

package com.welab.wefe.data.fusion.service.api.system;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.service.GlobalSettingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "system/initialize", name = "更新系统配置")
public class InitializeApi extends AbstractNoneOutputApi<InitializeApi.Input> {

    @Autowired
    private GlobalSettingService globalSettingService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        globalSettingService.initialize(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "名称")
        String partnerName;


        public String getPartnerName() {
            return partnerName;
        }

        public void setPartnerName(String partnerName) {
            this.partnerName = partnerName;
        }
    }
}
