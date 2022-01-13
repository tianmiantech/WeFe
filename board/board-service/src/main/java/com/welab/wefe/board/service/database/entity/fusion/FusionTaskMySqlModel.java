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

package com.welab.wefe.board.service.database.entity.fusion;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.fusion.core.enums.AlgorithmType;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import com.welab.wefe.fusion.core.enums.PSIActuatorRole;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
@Entity(name = "fusion_task")
public class FusionTaskMySqlModel extends AbstractBaseMySqlModel {

    String projectId;

    String businessId;

    String name;

    @Enumerated(EnumType.STRING)
    FusionTaskStatus status;

    String error;

    String dstMemberId;

    String dataResourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_resource_type")
    DataResourceType dataResourceType;

    /**
     * Number of rows of data resources
     */
    Long rowCount;

    String partnerDataResourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_data_resource_type")
    DataResourceType partnerDataResourceType;

    /**
     * Number of rows of data resources
     */
    Long parnterRowCount;

    /**
     * Whether the trace
     */
    public boolean isTrace;

    /**
     * Traces the field
     */
    public String traceColumn;


    @Enumerated(EnumType.STRING)
    @Column(name = "psi_actuator_role")
    PSIActuatorRole psiActuatorRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "my_role")
    JobMemberRole myRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm")
    AlgorithmType algorithm;


    /**
     * Number of fusion
     */
    public int fusionCount;

    public long spend;

    public String description;

    public String comment;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public Long getParnterRowCount() {
        return parnterRowCount;
    }

    public void setParnterRowCount(Long parnterRowCount) {
        this.parnterRowCount = parnterRowCount;
    }
}
