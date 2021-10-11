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

package com.welab.wefe.board.service.api.project.member;

import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author lonnie
 */
@Api(
        path = "project/member/online_check",
        name = "Check if the gateway and board of other members are connected"
)
public class OnlineCheckApi extends AbstractApi<OnlineCheckApi.Input, JObject> {

    @Autowired
    private ServiceCheckService serviceCheckService;

    @Override
    protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException {

        List<ServiceCheckService.GatewayOnlineCheckResult> checkResultList = serviceCheckService.gatewayOnlineCheck(input.isLocal(), input.getProjectId(), input.getMemberIds());

        return success(JObject.create().append("result", checkResultList));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "项目id")
        private String projectId;

        @Check(name = "是否是本地")
        private boolean local;

        private List<String> memberIds;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public boolean isLocal() {
            return local;
        }

        public void setLocal(boolean local) {
            this.local = local;
        }

        public List<String> getMemberIds() {
            return memberIds;
        }

        public void setMemberIds(List<String> memberIds) {
            this.memberIds = memberIds;
        }
    }
}
