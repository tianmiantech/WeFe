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
package com.welab.wefe.board.service.api.project.fusion.actuator.psi;



import com.welab.wefe.board.service.dto.fusion.PsiMeta;
import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.io.IOException;
import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(
        path = "fusion/psi/crypto",
        name = "psi crypto",
        desc = "psi crypto",
        login = false,
        rsaVerify = true
)
public class PsiCryptoApi extends AbstractApi<PsiCryptoApi.Input, PsiMeta> {


    @Override
    protected ApiResult<PsiMeta> handle(Input input) throws StatusCodeWithException, IOException {
        ServerActuator actuator = (ServerActuator) ActuatorManager.get(input.getBusinessId());
        if (actuator == null) {
            LOG.error("Actuator not found,businessId is {}", input.getBusinessId());
            throw new StatusCodeWithException("Actuator not found", StatusCode.DATA_NOT_FOUND);
        }

        return success(PsiMeta.of(actuator.compute(input.getBs())));
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "businessId", require = true)
        String businessId;

        @Check(name = "bs", blockReactionaryKeyword = false)
        List<String> bs;

        public Input(String businessId, List<String> bs) {
            this.businessId = businessId;
            this.bs = bs;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public List<String> getBs() {
            return bs;
        }

        public void setBs(List<String> bs) {
            this.bs = bs;
        }
    }
}
