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

package com.welab.wefe.union.service.service.flowlimit;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.constant.FlowLimitStrategyTypeEnum;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.FlowLimitByMobile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author aaron.li
 * @date 2021/11/9 11:05
 **/
public class FlowLimitByMobileService extends AbstractFlowLimitService {

    public FlowLimitByMobileService(HttpServletRequest httpServletRequest, AbstractApi<?, ?> api, JSONObject params) {
        super(httpServletRequest, api, params);
    }

    @Override
    protected String getFlowLimitKey() throws StatusCodeWithException {
        String mobile = getParams().getString("mobile");
        if (StringUtil.isEmpty(mobile)) {
            throw new StatusCodeWithException("手机号不能为空", StatusCode.PERMISSION_DENIED);
        }
        String path = getApi().getClass().getAnnotation(Api.class).path();
        return path + "_" + FlowLimitStrategyTypeEnum.Mobile + "_" + mobile;
    }

    @Override
    protected FlowLimitStrategyTypeEnum getFlowLimitStrategyType() {
        return FlowLimitStrategyTypeEnum.Mobile;
    }

    @Override
    protected String getFlowLimitStrategyValue() {
        return getParams().getString("mobile");
    }

    @Override
    protected int getFlowLimitCount() {
        return getApi().getClass().getAnnotation(FlowLimitByMobile.class).count();
    }

    @Override
    protected long getFlowLimitSecond() {
        return getApi().getClass().getAnnotation(FlowLimitByMobile.class).second();
    }

    @Override
    protected String getFlowLimitExceptionTips() {
        return "该手机号访问次数过于频繁，请稍后再试";
    }
}
