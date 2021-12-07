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

package com.welab.wefe.board.service.dto.fusion;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.BloomFilterOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.fusion.core.enums.AlgorithmType;
import com.welab.wefe.fusion.core.enums.DataResourceType;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;

import java.util.List;

;

/**
 * @author hunter.zhao
 */
public class FusionTaskOutput extends AbstractOutputModel {

    private String businessId;

    String name;

    FusionTaskStatus status;

    String error;

    String partnerId;

    String partnerName;

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
    public int dataCount;

    /**
     * Number of fusion
     */
    public int fusionCount;

    public long spend;


    private String description;

    /**
     * Data set list
     */
    private List<TableDataSetOutputModel> dataSetList;

    /**
     * bloomFilterList
     */
    private List<BloomFilterOutputModel> bloomFilterList;


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

    public FusionTaskStatus getStatus() {
        return status;
    }

    public void setStatus(FusionTaskStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
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

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public int getFusionCount() {
        return fusionCount;
    }

    public void setFusionCount(int fusionCount) {
        this.fusionCount = fusionCount;
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

    public List<TableDataSetOutputModel> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<TableDataSetOutputModel> dataSetList) {
        this.dataSetList = dataSetList;
    }

    public List<BloomFilterOutputModel> getBloomFilterList() {
        return bloomFilterList;
    }

    public void setBloomFilterList(List<BloomFilterOutputModel> bloomFilterList) {
        this.bloomFilterList = bloomFilterList;
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
}
