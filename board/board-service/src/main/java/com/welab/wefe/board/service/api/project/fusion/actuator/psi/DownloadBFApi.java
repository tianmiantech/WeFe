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
package com.welab.wefe.board.service.api.project.fusion.actuator.psi;



import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;
import org.apache.hadoop.hbase.util.BloomFilter;

import java.io.IOException;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/psi/download_bloom_filter",
        name = "download bloomfilter",
        desc = "download bloomfilter",
        login = false,
        rsaVerify = true
)
public class DownloadBFApi extends AbstractApi<DownloadBFApi.Input, PsiActuatorMeta> {

    @Override
    protected ApiResult<PsiActuatorMeta> handle(Input input) throws StatusCodeWithException, IOException {
        ServerActuator actuator = (ServerActuator) ActuatorManager.get(input.getBusinessId());
        if (actuator == null) {
            LOG.error("Actuator not found,businessId is {}", input.getBusinessId());
            throw new StatusCodeWithException("Actuator not found", StatusCode.DATA_NOT_FOUND);
        }

        return success(actuator.getActuatorParam());
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "businessId", require = true)
        String businessId;

        public Input(String businessId) {
            this.businessId = businessId;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }
    }

    public static void main(String[] args) {
        BloomFilters bf = new BloomFilters(0.01,1000000);
        System.out.println(bf.size());
    }
}
