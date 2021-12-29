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

package com.welab.wefe.board.service.api.member;

import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Check route connect of member gateway
 *
 * @author aaron.li
 **/
@Api(path = "member/check_route_connect", name = "Check route connect of member gateway", desc = "Check route connect of member gateway")
public class CheckMemberRouteConnectApi extends AbstractNoneOutputApi<CheckMemberRouteConnectApi.Input> {
    @Autowired
    private GatewayService gatewayService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        gatewayService.pingGatewayAlive(input.memberGatewayUri);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "Gateway IP:PORT. If the value is not empty, it means to directly test its own gateway alive")
        private String memberGatewayUri;

        public String getMemberGatewayUri() {
            return memberGatewayUri;
        }

        public void setMemberGatewayUri(String memberGatewayUri) {
            this.memberGatewayUri = memberGatewayUri;
        }
    }
}
