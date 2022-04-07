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
package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.config.CommonConfig;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.env.EnvBranch;
import com.welab.wefe.common.wefe.enums.env.EnvName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane
 * @date 2022/4/7
 */
@Api(path = "env", name = "环境变量")
public class EnvApi extends AbstractNoneInputApi<EnvApi.Output> {
    @Autowired
    private CommonConfig config;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        Output output = new Output();
        output.envName = config.getEnvName();
        output.envBranch = config.getEnvBranch();
        output.isDemo = config.getEnvBranch() == EnvBranch.online_demo;
        return null;
    }

    public class Output {
        @Check(name = "环境名称")
        public EnvName envName;
        @Check(name = "环境分支")
        public EnvBranch envBranch;
        @Check(name = "是否是 demo 环境")
        public boolean isDemo;
    }
}
