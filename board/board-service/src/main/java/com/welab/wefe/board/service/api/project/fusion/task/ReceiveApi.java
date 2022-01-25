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

package com.welab.wefe.board.service.api.project.fusion.task;

import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.fusion.core.enums.AlgorithmType;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "task/receive", name = "接收对齐请求", desc = "接收对齐请求", login = false, rsaVerify = true)
public class ReceiveApi extends AbstractNoneOutputApi<ReceiveApi.Input> {
    @Autowired
    FusionTaskService fusionTaskService;


    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        fusionTaskService.alignByPartner(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "指定操作的projectId", require = true)
        private String projectId;

        @Check(name = "指定操作的businessId", require = true)
        private String businessId;

        @Check(name = "任务名称", require = true)
        private String name;

        @Check(name = "合作方id", require = true)
        private String dstMemberId;

        @Check(name = "数据资源id", require = true)
        private String dataResourceId;

        @Check(name = "数据资源类型", require = true)
        private DataResourceType dataResourceType;

        @Check(name = "对方数据资源id", require = true)
        private String partnerDataResourceId;

        @Check(name = "数据资源类型", require = true)
        private DataResourceType partnerDataResourceType;

        @Check(name = "对方数据融合公式", require = true)
        private String partnerHashFunction;

        @Check(name = "数据资源的数据量")
        private Long rowCount;

        @Check(name = "合作方的数据资源的数据量")
        private Long partnerRowCount;

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

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getDstMemberId() {
            return dstMemberId;
        }

        public void setDstMemberId(String dstMemberId) {
            this.dstMemberId = dstMemberId;
        }

        public Long getRowCount() {
            return rowCount;
        }

        public void setRowCount(Long rowCount) {
            this.rowCount = rowCount;
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

        public String getDataResourceId() {
            return dataResourceId;
        }

        public void setDataResourceId(String dataResourceId) {
            this.dataResourceId = dataResourceId;
        }

        public DataResourceType getDataResourceType() {
            return dataResourceType;
        }

        public void setDataResourceType(DataResourceType dataResourceType) {
            this.dataResourceType = dataResourceType;
        }

        public String getPartnerDataResourceId() {
            return partnerDataResourceId;
        }

        public void setPartnerDataResourceId(String partnerDataResourceId) {
            this.partnerDataResourceId = partnerDataResourceId;
        }

        public DataResourceType getPartnerDataResourceType() {
            return partnerDataResourceType;
        }

        public void setPartnerDataResourceType(DataResourceType partnerDataResourceType) {
            this.partnerDataResourceType = partnerDataResourceType;
        }

        public Long getPartnerRowCount() {
            return partnerRowCount;
        }

        public void setPartnerRowCount(Long partnerRowCount) {
            this.partnerRowCount = partnerRowCount;
        }

        public String getPartnerHashFunction() {
            return partnerHashFunction;
        }

        public void setPartnerHashFunction(String partnerHashFunction) {
            this.partnerHashFunction = partnerHashFunction;
        }
    }
}
