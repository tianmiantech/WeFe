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

package com.welab.wefe.union.service.api.dataresource.dataset.nomal;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataSetPublicLevel;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.DataSetOutput;
import com.welab.wefe.union.service.entity.DataSet;
import com.welab.wefe.union.service.service.DataSetContractService;
import com.welab.wefe.union.service.service.DataSetMemberPermissionContractService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jervis
 **/
@Api(path = "data_set/put", name = "data_set_put", rsaVerify = true, login = false)
public class PutApi extends AbstractApi<PutApi.Input, DataSetOutput> {
    @Autowired
    protected DataSetContractService mDataSetContractService;


    @Autowired
    private DataSetMemberPermissionContractService mDataSetMemberPermissionContractService;

    @Override
    protected ApiResult<DataSetOutput> handle(PutApi.Input input) throws StatusCodeWithException {

        DataSet dataSet = new DataSet();
        BeanUtils.copyProperties(input, dataSet);
        dataSet.setContainsY(input.containsY ? 1 : 0);

        String publicMemberList = input.publicMemberList;

        if (DataSetPublicLevel.OnlyMyself.name().equals(input.publicLevel)) {
            mDataSetMemberPermissionContractService.deleteByDataSetId(dataSet.getId());
            dataSet.setPublicLevel(input.publicLevel);

        } else if (DataSetPublicLevel.Public.name().equals(input.publicLevel)) {
            mDataSetMemberPermissionContractService.deleteByDataSetId(dataSet.getId());
            dataSet.setPublicLevel(input.publicLevel);

        } else if (DataSetPublicLevel.PublicWithMemberList.name().equals(input.publicLevel)) {
            mDataSetMemberPermissionContractService.save(dataSet.getId(), publicMemberList);
            dataSet.setPublicLevel(input.publicLevel);

        } else {
            throw new StatusCodeWithException("Invalid public level", StatusCode.SYSTEM_ERROR);
        }

        mDataSetContractService.upsert(dataSet);

        return success();
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private String id;
        @Check(require = true)
        private String name;
        @Check(require = true)
        private String memberId;
        private Boolean containsY;
        private Long rowCount;
        private Integer columnCount;
        private String columnNameList;
        private Integer featureCount;
        private String featureNameList;
        @Check(require = true)
        private String publicLevel;
        private String publicMemberList;
        private int usageCountInJob;
        private int usageCountInFlow;
        private int usageCountInProject;
        private String description;
        @Check(require = true)
        private String tags;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public Boolean getContainsY() {
            return containsY;
        }

        public void setContainsY(Boolean containsY) {
            this.containsY = containsY;
        }

        public Long getRowCount() {
            return rowCount;
        }

        public void setRowCount(Long rowCount) {
            this.rowCount = rowCount;
        }

        public Integer getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(Integer columnCount) {
            this.columnCount = columnCount;
        }

        public String getColumnNameList() {
            return columnNameList;
        }

        public void setColumnNameList(String columnNameList) {
            this.columnNameList = columnNameList;
        }

        public Integer getFeatureCount() {
            return featureCount;
        }

        public void setFeatureCount(Integer featureCount) {
            this.featureCount = featureCount;
        }

        public String getFeatureNameList() {
            return featureNameList;
        }

        public void setFeatureNameList(String featureNameList) {
            this.featureNameList = featureNameList;
        }

        public String getPublicLevel() {
            return publicLevel;
        }

        public void setPublicLevel(String publicLevel) {
            this.publicLevel = publicLevel;
        }

        public String getPublicMemberList() {
            return publicMemberList;
        }

        public void setPublicMemberList(String publicMemberList) {
            this.publicMemberList = publicMemberList;
        }

        public int getUsageCountInJob() {
            return usageCountInJob;
        }

        public void setUsageCountInJob(int usageCountInJob) {
            this.usageCountInJob = usageCountInJob;
        }

        public int getUsageCountInFlow() {
            return usageCountInFlow;
        }

        public void setUsageCountInFlow(int usageCountInFlow) {
            this.usageCountInFlow = usageCountInFlow;
        }

        public int getUsageCountInProject() {
            return usageCountInProject;
        }

        public void setUsageCountInProject(int usageCountInProject) {
            this.usageCountInProject = usageCountInProject;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }
    }
}
