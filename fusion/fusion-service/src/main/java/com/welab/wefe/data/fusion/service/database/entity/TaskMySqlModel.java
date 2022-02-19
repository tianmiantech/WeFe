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

package com.welab.wefe.data.fusion.service.database.entity;

import com.welab.wefe.data.fusion.service.enums.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
@Entity(name = "task")
public class TaskMySqlModel extends AbstractBaseMySqlModel {

    String businessId;

    String name;

    @Enumerated(EnumType.STRING)
    TaskStatus status;

    String error;

    String partnerMemberId;

    String dataResourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_resource_type")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "psi_actuator_role")
    PSIActuatorRole psiActuatorRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm")
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

    public String description;


    @Enumerated(EnumType.STRING)
    @Column(name = "my_role")
    public RoleType myRole;


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

    public RoleType getMyRole() {
        return myRole;
    }

    public void setMyRole(RoleType myRole) {
        this.myRole = myRole;
    }
}
