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

package com.welab.wefe.board.service.api.project.fusion.task;

import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.board.service.util.primarykey.FieldInfo;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.fusion.core.enums.AlgorithmType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/task/add", name = "添加对齐任务", desc = "添加对齐任务")
public class AddApi extends AbstractNoneOutputApi<AddApi.Input> {

    @Autowired
    FusionTaskService fusionTaskService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        fusionTaskService.add(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "项目id", require = true)
        private String projectId;

        @Check(name = "任务名称", require = true, regex = "^.{4,40}$", messageOnInvalid = "任务名称长度不能少于4，不能大于40")
        private String name;

        @Check(name = "描述", regex = "^.{0,1024}$", messageOnInvalid = "你写的描述太多了~")
        private String description;

        @Check(name = "合作方id", require = true)
        private String dstMemberId;

        @Check(name = "数据资源id", require = true)
        private String dataResourceId;

        @Check(name = "数据资源类型", require = true)
        private DataResourceType dataResourceType;

        @Check(name = "样本量", require = true)
        private Long rowCount;

        @Check(name = "对方数据资源id", require = true)
        private String partnerDataResourceId;

        @Check(name = "对方数据资源类型", require = true)
        private DataResourceType partnerDataResourceType;

        @Check(name = "对方样本量", require = true)
        private Long partnerRowCount;

        @Check(name = "算法", require = true)
        private AlgorithmType algorithm;

        @Check(name = "主键处理")
        private List<FieldInfo> fieldInfoList;

        @Check(name = "是否追溯", require = true)
        private Boolean isTrace;

        @Check(name = "追溯字段")
        private String traceColumn;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (DataResourceType.TableDataSet.equals(dataResourceType)
                    && fieldInfoList.isEmpty()) {
                throw new StatusCodeWithException("请设置主键", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (isTrace && StringUtil.isEmpty(traceColumn)) {
                throw new StatusCodeWithException("追溯字段不能为空", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (AlgorithmType.RSA_PSI.equals(algorithm) && partnerDataResourceType.equals(dataResourceType)) {
                throw new StatusCodeWithException(" RSA-PSI 算法要求至少一方需要选择布隆过滤器资源, 另一方则必须为数据资源资源！", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (isTrace && CollectionUtils.isNotEmpty(fieldInfoList)) {
                for (int i = 0; i < fieldInfoList.size(); i++) {
                    if (fieldInfoList.get(i).getColumnList().contains(traceColumn)) {
                        throw new StatusCodeWithException("追溯字段不能为融合主键组成字段", StatusCode.PARAMETER_VALUE_INVALID);
                    }
                }
            }

        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDstMemberId() {
            return dstMemberId;
        }

        public void setDstMemberId(String dstMemberId) {
            this.dstMemberId = dstMemberId;
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

        public AlgorithmType getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(AlgorithmType algorithm) {
            this.algorithm = algorithm;
        }

        public Long getRowCount() {
            return rowCount;
        }

        public void setRowCount(Long rowCount) {
            this.rowCount = rowCount;
        }

        public List<FieldInfo> getFieldInfoList() {
            return fieldInfoList;
        }

        public void setFieldInfoList(List<FieldInfo> fieldInfoList) {
            this.fieldInfoList = fieldInfoList;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getTrace() {
            return isTrace;
        }

        public void setTrace(Boolean trace) {
            isTrace = trace;
        }

        public String getTraceColumn() {
            return traceColumn;
        }

        public void setTraceColumn(String traceColumn) {
            this.traceColumn = traceColumn;
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
    }
}
