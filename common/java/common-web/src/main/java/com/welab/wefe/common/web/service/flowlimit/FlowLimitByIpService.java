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

package com.welab.wefe.common.web.service.flowlimit;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.FlowLimitByIp;
import com.welab.wefe.common.web.util.HttpServletRequestUtil;
import com.welab.wefe.common.wefe.enums.FlowLimitStrategyTypeEnum;

import javax.servlet.http.HttpServletRequest;

/**
 * Flow control of basic IP address
 **/
public class FlowLimitByIpService extends AbstractMemoryFlowLimitService {
    public FlowLimitByIpService(HttpServletRequest httpServletRequest, AbstractApi<?, ?> api, JSONObject params) {
        super(httpServletRequest, api, params);
    }

    @Override
    protected String getFlowLimitKey() throws StatusCodeWithException {
        String clientIp = HttpServletRequestUtil.getClientIp(getHttpServletRequest());
        String path = getApi().getClass().getAnnotation(Api.class).path();
        return path + "_" + FlowLimitStrategyTypeEnum.IP + "_" + clientIp;
    }

    @Override
    protected FlowLimitStrategyTypeEnum getFlowLimitStrategyType() {
        return FlowLimitStrategyTypeEnum.IP;
    }

    @Override
    protected String getFlowLimitStrategyValue() {
        return HttpServletRequestUtil.getClientIp(getHttpServletRequest());
    }

    @Override
    protected long getFlowLimitSecond() {
        return getApi().getClass().getAnnotation(FlowLimitByIp.class).second();
    }

    @Override
    protected int getFlowLimitCount() {
        return getApi().getClass().getAnnotation(FlowLimitByIp.class).count();
    }

    @Override
    protected String getFlowLimitExceptionTips() {
        return "该IP访问次数过于频繁，请稍后再试";
    }
}
