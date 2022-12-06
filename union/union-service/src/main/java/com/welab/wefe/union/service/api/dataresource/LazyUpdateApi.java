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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.DataSetContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "data_resource/lazy_update", name = "image_data_set_update_labeled_count", allowAccessWithSign = true)
public class LazyUpdateApi extends AbstractApi<LazyUpdateApi.Input, AbstractApiOutput> {
    @Autowired
    private DataSetContractService dataSetContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        dataSetContractService.lazyUpdate(input);
        return success();
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private DataResourceType dataResourceType;
        @Check(require = true)
        private String dataResourceId;
        @Check(require = true)
        private Integer totalDataCount;
        private String labelList;
        private Integer labeledCount;
        private Integer usageCountInJob;
        private Integer usageCountInFlow;
        private Integer usageCountInProject;
        private Integer usageCountInMember;
        private Boolean labelCompleted;

        public DataResourceType getDataResourceType() {
            return dataResourceType;
        }

        public void setDataResourceType(DataResourceType dataResourceType) {
            this.dataResourceType = dataResourceType;
        }

        public String getDataResourceId() {
            return dataResourceId;
        }

        public void setDataResourceId(String dataResourceId) {
            this.dataResourceId = dataResourceId;
        }

        public Integer getTotalDataCount() {
            return totalDataCount;
        }

        public void setTotalDataCount(Integer totalDataCount) {
            this.totalDataCount = totalDataCount;
        }

        public String getLabelList() {
            return labelList;
        }

        public void setLabelList(String labelList) {
            this.labelList = labelList;
        }

        public Integer getLabeledCount() {
            return labeledCount;
        }

        public void setLabeledCount(Integer labeledCount) {
            this.labeledCount = labeledCount;
        }

        public Integer getUsageCountInJob() {
            return usageCountInJob;
        }

        public void setUsageCountInJob(Integer usageCountInJob) {
            this.usageCountInJob = usageCountInJob;
        }

        public Integer getUsageCountInFlow() {
            return usageCountInFlow;
        }

        public void setUsageCountInFlow(Integer usageCountInFlow) {
            this.usageCountInFlow = usageCountInFlow;
        }

        public Integer getUsageCountInProject() {
            return usageCountInProject;
        }

        public void setUsageCountInProject(Integer usageCountInProject) {
            this.usageCountInProject = usageCountInProject;
        }

        public Integer getUsageCountInMember() {
            return usageCountInMember;
        }

        public void setUsageCountInMember(Integer usageCountInMember) {
            this.usageCountInMember = usageCountInMember;
        }

        public Boolean getLabelCompleted() {
            return labelCompleted;
        }

        public void setLabelCompleted(Boolean labelCompleted) {
            this.labelCompleted = labelCompleted;
        }
    }
}
