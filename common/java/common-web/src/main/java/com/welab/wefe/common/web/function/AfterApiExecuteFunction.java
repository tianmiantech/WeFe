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

package com.welab.wefe.common.web.function;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.dto.ApiResult;

import javax.servlet.http.HttpServletRequest;

/**
 * Events triggered after API execution
 *
 * @author zane.luo
 */
@FunctionalInterface
public interface AfterApiExecuteFunction {

    /**
     * @param api
     * @param params
     * @param result
     */
    void action(HttpServletRequest httpServletRequest, long start, AbstractApi<?, ?> api, JSONObject params, ApiResult<?> result);
}
