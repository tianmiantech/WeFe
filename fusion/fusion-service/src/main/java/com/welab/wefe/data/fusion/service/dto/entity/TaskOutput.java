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

package com.welab.wefe.data.fusion.service.dto.entity;

import com.welab.wefe.data.fusion.service.dto.entity.bloomfilter.BloomfilterOutputModel;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetOutputModel;
import com.welab.wefe.data.fusion.service.enums.*;

import java.util.List;

/**
 * @author hunter.zhao
 */
public class TaskOutput extends AbstractOutputModel {

    private String businessId;

    String name;

    TaskStatus status;

    String error;

    String partnerMemberId;

    String partnerMemberName;

    String dataResourceId;

    String dataResourceName;

    DataResourceType dataResourceType;

    /**
     * Whether the trace
     */
    public boolean isTrace;

    /**
     * Traces the field
     */
    public String traceColumn;

    /**
     * Number of rows of data resources
     */
    int rowCount;

    PSIActuatorRole psiActuatorRole;

    AlgorithmType algorithm;

    /**
     * Number of aligned samples
     */
    public Integer dataCount;

    /**
     * Number of fusion
     */
    public Integer fusionCount;

    /**
     * Number of processed
     */
    public Integer processedCount;

    public long spend;


    private String description;

    /**
     * Data set list
     */
    private List<DataSetOutputModel> dataSetList;

    /**
     * bloomFilterList
     */
    private List<BloomfilterOutputModel> bloomFilterList;

    /**
     * partners
     */
    private List<PartnerOutputModel> partnerList;

    /**
     * my_role
     */
    private RoleType myRole;

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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPartnerMemberId() {
        return partnerMemberId;
    }

    public void setPartnerMemberId(String partnerMemberId) {
        this.partnerMemberId = partnerMemberId;
    }

    public String getPartnerMemberName() {
        return partnerMemberName;
    }

    public void setPartnerMemberName(String partnerMemberName) {
        this.partnerMemberName = partnerMemberName;
    }

    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
    }

    public String getDataResourceName() {
        return dataResourceName;
    }

    public void setDataResourceName(String dataResourceName) {
        this.dataResourceName = dataResourceName;
    }

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public boolean isTrace() {
        return isTrace;
    }

    public void setTrace(boolean trace) {
        isTrace = trace;
    }

    public String getTraceColumn() {
        return traceColumn;
    }

    public void setTraceColumn(String traceColumn) {
        this.traceColumn = traceColumn;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
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

    public Integer getDataCount() {
        return dataCount;
    }

    public void setDataCount(Integer dataCount) {
        this.dataCount = dataCount;
    }

    public Integer getFusionCount() {
        return fusionCount;
    }

    public void setFusionCount(Integer fusionCount) {
        this.fusionCount = fusionCount;
    }

    public Integer getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Integer processedCount) {
        this.processedCount = processedCount;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DataSetOutputModel> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<DataSetOutputModel> dataSetList) {
        this.dataSetList = dataSetList;
    }

    public List<BloomfilterOutputModel> getBloomFilterList() {
        return bloomFilterList;
    }

    public void setBloomFilterList(List<BloomfilterOutputModel> bloomFilterList) {
        this.bloomFilterList = bloomFilterList;
    }

    public List<PartnerOutputModel> getPartnerList() {
        return partnerList;
    }

    public void setPartnerList(List<PartnerOutputModel> partnerList) {
        this.partnerList = partnerList;
    }

    public RoleType getMyRole() {
        return myRole;
    }

    public void setMyRole(RoleType myRole) {
        this.myRole = myRole;
    }

    public String getResultTable() {
        if (TaskStatus.Success.equals(status) && fusionCount > 0) {
            return "task_result_" + businessId;
        }

        return "";
    }
}
