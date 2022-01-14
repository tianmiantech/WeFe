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

package com.welab.wefe.board.service.dto.fusion;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.BloomFilterOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.fusion.core.enums.AlgorithmType;
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

    FusionMemberInfo promoter;

    FusionMemberInfo provider;

    JobMemberRole myRole;

    String dstMemberId;

    String dataResourceId;

    String dataResourceName;

    DataResourceType dataResourceType;


    @Check(name = "Number of rows of data resources")
    Long rowCount;

    String partnerDataResourceId;

    String partnerDataResourceName;

    DataResourceType partnerDataResourceType;

    @Check(name = "Number of rows of data resources")
    public Long partnerRowCount;

    @Check(name = "Whether the trace")
    public boolean isTrace;

    @Check(name = "Traces the field")
    public String traceColumn;

    PSIActuatorRole psiActuatorRole;

    AlgorithmType algorithm;

    @Check(name = "Number of fusion")
    public int fusionCount;

    public long spend;


    private String description;


    public String comment;


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

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public Long getPartnerRowCount() {
        return partnerRowCount;
    }

    public void setPartnerRowCount(Long partnerRowCount) {
        this.partnerRowCount = partnerRowCount;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public FusionMemberInfo getPromoter() {
        return promoter;
    }

    public void setPromoter(FusionMemberInfo promoter) {
        this.promoter = promoter;
    }

    public FusionMemberInfo getProvider() {
        return provider;
    }

    public void setProvider(FusionMemberInfo provider) {
        this.provider = provider;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public String getPartnerDataResourceId() {
        return partnerDataResourceId;
    }

    public void setPartnerDataResourceId(String partnerDataResourceId) {
        this.partnerDataResourceId = partnerDataResourceId;
    }

    public String getPartnerDataResourceName() {
        return partnerDataResourceName;
    }

    public void setPartnerDataResourceName(String partnerDataResourceName) {
        this.partnerDataResourceName = partnerDataResourceName;
    }

    public DataResourceType getPartnerDataResourceType() {
        return partnerDataResourceType;
    }

    public void setPartnerDataResourceType(DataResourceType partnerDataResourceType) {
        this.partnerDataResourceType = partnerDataResourceType;
    }

    public String getDstMemberId() {
        return dstMemberId;
    }

    public void setDstMemberId(String dstMemberId) {
        this.dstMemberId = dstMemberId;
    }
}
