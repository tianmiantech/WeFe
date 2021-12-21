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

package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * @author hunter.zhao
 */
@Entity(name = "predict_log")
public class PredictLogMySqlModel {
    /**
     * Globally unique identification
     */
    @Id
    @Column(name = "id", updatable = false)
    private String id = UUID.randomUUID().toString().replaceAll("-", "");

    @Column(name = "seq_no")
    private String seqNo;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "model_id")
    private String modelId;

    @Enumerated(EnumType.STRING)
    private Algorithm algorithm;

    @Enumerated(EnumType.STRING)
    @Column(name = "fl_type")
    private FederatedLearningType flType;

    @Enumerated(EnumType.STRING)
    @Column(name = "my_role")
    private JobMemberRole myRole;

    @Column(name = "created_time")
    private Date createdTime = new Date();

    private String request;

    private String response;

    private long spend;

    private boolean result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }

    public FederatedLearningType getFlType() {
        return flType;
    }

    public void setFlType(FederatedLearningType flType) {
        this.flType = flType;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
