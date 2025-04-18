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

package com.welab.wefe.serving.service.database.entity;

import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.service.enums.MemberModelStatusEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
@Entity(name = "model_member")
public class ModelMemberMySqlModel extends AbstractBaseMySqlModel {

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "member_id")
    private String memberId;

    @Enumerated(EnumType.STRING)
    private JobMemberRole role;

    @Enumerated(EnumType.STRING)
    private MemberModelStatusEnum status = MemberModelStatusEnum.offline;


    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public JobMemberRole getRole() {
        return role;
    }

    public void setRole(JobMemberRole role) {
        this.role = role;
    }

    public MemberModelStatusEnum getStatus() {
        return status;
    }

    public void setStatus(MemberModelStatusEnum status) {
        this.status = status;
    }
}
