package com.welab.wefe.board.service.api.fusion.actuator.psi;

/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.io.IOException;

/**
 * @author hunter.zhao
 */
@Api(
        path = "fusion/psi/handle",
        name = "psi handle",
        desc = "psi handle",
        login = false,
        rsaVerify = true)
public class PsiHandleApi extends AbstractApi<PsiHandleApi.Input, byte[][]> {


    @Override
    protected ApiResult<byte[][]> handle(Input input) throws StatusCodeWithException, IOException {
        ServerActuator actuator = (ServerActuator) ActuatorManager.get(input.getBusinessId());

        return success(actuator.compute(input.getBs()));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "businessId", require = true)
        String businessId;

        byte[][] bs;

        public Input(String businessId, byte[][] bs) {
            this.businessId = businessId;
            this.bs = bs;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public byte[][] getBs() {
            return bs;
        }

        public void setBs(byte[][] bs) {
            this.bs = bs;
        }
    }
}
