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

package com.welab.wefe.board.service.api.gateway;


import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.ApiExecutor;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.GatewayMemberInfo;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author zane.luo
 */
@Api(path = "gateway/redirect", name = "Redirect requests from gateway to internal api", login = false, rsaVerify = true)
public class RedirectApi extends AbstractApi<RedirectApi.Input, Object> {

    @Override
    protected ApiResult<Object> handle(Input input) throws StatusCodeWithException {

        AbstractApi api = Launcher.CONTEXT.getBean(input.api, AbstractApi.class);
        // join the requester's member information in the input
        input.data.put(
                "callerMemberInfo",
                new GatewayMemberInfo(input.callerMemberId, input.callerMemberName, input.callerMemberRole)
        );

        ApiResult result = api.execute("gateway", JObject.create(input.data));

        // 由于这个 api 对象由 RedirectApi 调用，没有走 ApiExecutor，会导致没有响应日志，所以在这里补一个日志。
        Api annotation = api.getClass().getAnnotation(Api.class);
        ApiExecutor.logResponse(annotation, result);

        return result;

    }

    public static class Input extends AbstractApiInput {
        private String callerMemberId;
        private String callerMemberName;
        private JobMemberRole callerMemberRole;
        private String api;
        private Map<String, Object> data;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (data == null) {
                data = new TreeMap<>();
            }
        }

        //region getter/setter

        public String getCallerMemberId() {
            return callerMemberId;
        }

        public void setCallerMemberId(String callerMemberId) {
            this.callerMemberId = callerMemberId;
        }

        public String getCallerMemberName() {
            return callerMemberName;
        }

        public void setCallerMemberName(String callerMemberName) {
            this.callerMemberName = callerMemberName;
        }

        public JobMemberRole getCallerMemberRole() {
            return callerMemberRole;
        }

        public void setCallerMemberRole(JobMemberRole callerMemberRole) {
            this.callerMemberRole = callerMemberRole;
        }

        public String getApi() {
            return api;
        }

        public void setApi(String api) {
            this.api = api;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }


        //endregion

    }
}
