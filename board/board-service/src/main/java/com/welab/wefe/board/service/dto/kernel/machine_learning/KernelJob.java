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

package com.welab.wefe.board.service.dto.kernel.machine_learning;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.FederatedLearningModel;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;

import java.util.List;

/**
 * @author zane.luo
 */
public class KernelJob {
    private FederatedLearningType federatedLearningType;
    private Project project;
    private Env env;
    private List<Member> members;
    private List<JobDataSet> dataSets;
    @Check(name = "Mixed Federation promoter_id")
    private String mixPromoterMemberId;
    private FederatedLearningModel federatedLearningMode;

    public JSONObject toJson() {
        return JSONObject.parseObject(JSONObject.toJSONString(this));
    }

    //region getter/setter


    public FederatedLearningType getFederatedLearningType() {
        return federatedLearningType;
    }

    public void setFederatedLearningType(FederatedLearningType federatedLearningType) {
        this.federatedLearningType = federatedLearningType;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Env getEnv() {
        return env;
    }

    public void setEnv(Env env) {
        this.env = env;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<JobDataSet> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<JobDataSet> dataSets) {
        this.dataSets = dataSets;
    }

    public String getMixPromoterMemberId() {
        return mixPromoterMemberId;
    }

    public void setMixPromoterMemberId(String mixPromoterMemberId) {
        this.mixPromoterMemberId = mixPromoterMemberId;
    }

    public FederatedLearningModel getFederatedLearningMode() {
        return federatedLearningMode;
    }

    public void setFederatedLearningMode(FederatedLearningModel federatedLearningMode) {
        this.federatedLearningMode = federatedLearningMode;
    }

    //endregion
}
