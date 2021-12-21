package com.welab.wefe.board.service.api.fusion.actuator.psi;

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


import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(
        path = "fusion/receive/result",
        name = "receive result",
        desc = "receive result",
        login = false,
        rsaVerify = true
)
public class ReceiveResultApi extends AbstractNoneOutputApi<ReceiveResultApi.Input> {


    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        ServerActuator actuator = (ServerActuator) ActuatorManager.get(input.getBusinessId());
        if (actuator == null) {
            LOG.error("Actuator not found,businessId is {}", input.getBusinessId());
            throw new StatusCodeWithException("Actuator not found", StatusCode.DATA_NOT_FOUND);
        }

        actuator.receiveResult(input.getRs());
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "businessId", require = true)
        String businessId;

        @Check(name = "rs")
        List<String> rs;

        public Input(String businessId, List<String> rs) {
            this.businessId = businessId;
            this.rs = rs;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public List<String> getRs() {
            return rs;
        }

        public void setRs(List<String> rs) {
            this.rs = rs;
        }

    }
}
