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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.AlgorithmType;
import com.welab.wefe.data.fusion.service.enums.DataResourceType;
import com.welab.wefe.data.fusion.service.service.TaskService;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(path = "task/add", name = "添加对齐任务", desc = "添加对齐任务")
public class AddApi extends AbstractNoneOutputApi<AddApi.Input> {

    @Autowired
    TaskService taskService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        taskService.add(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "任务名称", require = true, regex = "^.{4,40}$", messageOnInvalid = "任务名称长度不能少于4，不能大于40")
        private String name;

        @Check(name = "描述", regex = "^[\\s\\S]{0,1024}$", messageOnInvalid = "你写的描述太多了~")
        private String description;

        @Check(name = "合作方成员id")
        private String partnerMemberId;

        @Check(name = "数据资源id")
        private String dataResourceId;

        @Check(name = "数据资源类型")
        private DataResourceType dataResourceType;

        @Check(name = "算法")
        private AlgorithmType algorithm = AlgorithmType.RSA_PSI;

        @Check(name = "样本量")
        private Integer rowCount;

        @Check(name = "主键处理")
        private List<FieldInfo> fieldInfoList;

        @Check(name = "是否追溯")
        private Boolean isTrace;

        @Check(name = "追溯字段")
        private String traceColumn;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (StringUtil.isEmpty(dataResourceId)) {
                throw new StatusCodeWithException("请选择数据样本", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (StringUtil.isEmpty(partnerMemberId)) {
                throw new StatusCodeWithException("请选择合作方", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (DataResourceType.DataSet.equals(dataResourceType) && fieldInfoList.isEmpty()) {
                throw new StatusCodeWithException("请设置主键", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (isTrace && StringUtil.isEmpty(traceColumn)) {
                throw new StatusCodeWithException("追溯字段不能为空", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (isTrace && CollectionUtils.isNotEmpty(fieldInfoList)) {
                for (int i = 0; i < fieldInfoList.size(); i++) {
                    if (fieldInfoList.get(i).getColumnList().contains(traceColumn)) {
                        throw new StatusCodeWithException("追溯字段不能为融合主键组成字段", StatusCode.PARAMETER_VALUE_INVALID);
                    }
                }
            }

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

        public Integer getRowCount() {
            return rowCount;
        }

        public void setRowCount(Integer rowCount) {
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
    }
}
