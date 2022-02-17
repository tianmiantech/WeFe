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

package com.welab.wefe.data.fusion.service.api.task;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.AlgorithmType;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorRole;
import com.welab.wefe.data.fusion.service.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "task/receive", name = "接收对齐请求", desc = "接收对齐请求", login = false, rsaVerify = true)
public class ReceiveApi extends AbstractNoneOutputApi<ReceiveApi.Input> {
    @Autowired
    TaskService taskService;


    @Override
    protected ApiResult handler(ReceiveApi.Input input) throws StatusCodeWithException {
        taskService.alignByPartner(input);
        return success();
    }

    public static class Input extends AbstractApiInput {


        @Check(name = "指定操作的businessId", require = true)
        private String businessId;

        @Check(name = "任务名称", require = true)
        private String name;

        @Check(name = "合作方成员id", require = true)
        private String partnerMemberId;

        @Check(name = "数据集的数据量")
        private int dataCount;

        @Check(name = "对齐角色", require = true)
        private PSIActuatorRole psiActuatorRole;

        @Check(name = "算法", require = true)
        private AlgorithmType algorithm;

        @Check(name = "描述", regex = "^.{0,1024}$", messageOnInvalid = "你写的描述太多了~")
        private String description;


        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPartnerMemberId() {
            return partnerMemberId;
        }

        public void setPartnerMemberId(String partnerMemberId) {
            this.partnerMemberId = partnerMemberId;
        }

        public int getDataCount() {
            return dataCount;
        }

        public void setDataCount(int dataCount) {
            this.dataCount = dataCount;
        }

        public PSIActuatorRole getPsiActuatorRole() {
            return psiActuatorRole;
        }

        public void setPsiActuatorRole(PSIActuatorRole psiActuatorRole) {
            this.psiActuatorRole = psiActuatorRole;
        }

        public AlgorithmType getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(AlgorithmType algorithm) {
            this.algorithm = algorithm;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
