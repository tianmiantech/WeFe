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
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flow limit of basic memory
 */
public abstract class AbstractMemoryFlowLimitService extends AbstractFlowLimitService {
    /**
     * Flow limit record cache
     */
    private final static ConcurrentHashMap<String, AbstractFlowLimitService.FlowLimit> FLOW_LIMIT_CACHE = new ConcurrentHashMap<>(16);

    static {
        // Empty expired cache
        new Thread(() -> {
            while (true) {
                Iterator<Map.Entry<String, AbstractFlowLimitService.FlowLimit>> it = FLOW_LIMIT_CACHE.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, AbstractFlowLimitService.FlowLimit> entry = it.next();
                    AbstractFlowLimitService.FlowLimit flowLimit = entry.getValue();
                    if ((System.currentTimeMillis() - flowLimit.getLatestVisitTime()) > flowLimit.getActiveTime()) {
                        it.remove();
                    }
                }
                ThreadUtil.sleepSeconds(30);
            }
        }).start();
    }

    public AbstractMemoryFlowLimitService(HttpServletRequest httpServletRequest, AbstractApi<?, ?> api, JSONObject params) {
        super(httpServletRequest, api, params);
    }

    @Override
    protected FlowLimit getFlowLimit(String key) {
        return FLOW_LIMIT_CACHE.get(key);
    }

    @Override
    protected void updateFlowLimit(String key, FlowLimit flowLimit) {
        FLOW_LIMIT_CACHE.put(key, flowLimit);
    }
}
