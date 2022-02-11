/**
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

package com.welab.wefe.union.service.service.flowlimit;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.constant.FlowLimitStrategyTypeEnum;
import com.welab.wefe.common.data.mongodb.entity.common.FlowLimit;
import com.welab.wefe.common.data.mongodb.repo.FlowLimitRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;

import javax.servlet.http.HttpServletRequest;

/**
 * @author aaron.li
 * @date 2021/10/22 15:38
 **/
public abstract class AbstractFlowLimitService {

    /**
     * Http request object
     */
    private HttpServletRequest httpServletRequest;
    /**
     * API object
     */
    private AbstractApi<?, ?> api;
    /**
     * API request parameters
     */
    private JSONObject params;

    public AbstractFlowLimitService(HttpServletRequest httpServletRequest, AbstractApi<?, ?> api, JSONObject params) {
        this.httpServletRequest = httpServletRequest;
        this.api = api;
        this.params = params;
    }


    public void check() throws StatusCodeWithException {
        synchronized (AbstractFlowLimitService.class) {
            String key = getFlowLimitKey();
            FlowLimitRepo flowLimitRepo = Launcher.CONTEXT.getBean(FlowLimitRepo.class);
            FlowLimit flowLimit = flowLimitRepo.findByKey(key);
            flowLimit = (null == flowLimit ? createFlowLimit() : flowLimit);
            if ((System.currentTimeMillis() - flowLimit.getStartVisitTime()) <= (getFlowLimitSecond() * 1000L)) {
                if (flowLimit.getCount() >= getFlowLimitCount()) {
                    throw new StatusCodeWithException(getFlowLimitExceptionTips(), StatusCode.PERMISSION_DENIED);
                }
                flowLimit.setCount(flowLimit.getCount() + 1);
            } else {
                // Restore site
                flowLimit.setStartVisitTime(System.currentTimeMillis());
                flowLimit.setCount(1);
            }
            flowLimit.setLatestVisitTime(System.currentTimeMillis());
            flowLimitRepo.save(flowLimit);
        }
    }

    /**
     * The flow restriction key value must be globally unique
     */
    protected abstract String getFlowLimitKey() throws StatusCodeWithException;

    /**
     * Flow limit strategy type
     */
    protected abstract FlowLimitStrategyTypeEnum getFlowLimitStrategyType();

    /**
     *  Flow limit strategy value
     */
    protected abstract String getFlowLimitStrategyValue();

    /**
     * Flow limit duration, in seconds
     */
    protected abstract long getFlowLimitSecond();

    /**
     * Number of accessible api within the current limit duration
     */
    protected abstract int getFlowLimitCount();

    /**
     * Exception prompt information when triggering current limiting rules
     */
    protected String getFlowLimitExceptionTips() {
        return "访问次数过于频繁，请稍后再试";
    }

    /**
     * 返回初始化的流量对象
     */
    private FlowLimit createFlowLimit() throws StatusCodeWithException {
        FlowLimit flowLimit = new FlowLimit();
        flowLimit.setKey(getFlowLimitKey());
        flowLimit.setCount(0);
        flowLimit.setPath(api.getClass().getAnnotation(Api.class).path());
        flowLimit.setStrategyType(getFlowLimitStrategyType());
        flowLimit.setStrategyValue(getFlowLimitStrategyValue());
        flowLimit.setStartVisitTime(System.currentTimeMillis());
        flowLimit.setLatestVisitTime(flowLimit.getStartVisitTime());
        flowLimit.setActiveTime(getFlowLimitSecond() * 10 * 1000L);
        return flowLimit;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public AbstractApi<?, ?> getApi() {
        return api;
    }

    public void setApi(AbstractApi<?, ?> api) {
        this.api = api;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }
}
