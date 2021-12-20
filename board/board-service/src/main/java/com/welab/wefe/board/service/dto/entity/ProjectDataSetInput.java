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

package com.welab.wefe.board.service.dto.entity;


import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

/**
 * @author zane.luo
 */
public class ProjectDataSetInput extends AbstractCheckModel {
    @Check(name = "成员Id", require = true, messageOnEmpty = "请选择项目合作方")
    private String memberId;
    @Check(name = "成员角色", require = true, desc = "由于存在自己和自己联邦的情况，所以需要加上角色用于区分。")
    private JobMemberRole memberRole;

    @Check(name = "数据集 Id", require = true)
    private String dataSetId;

    @Check(name = "数据集类型", require = true)
    private DataResourceType dataResourceType;


    //region getter/setter


    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public DataResourceType getDataSetType() {
        return dataResourceType;
    }

    public void setDataSetType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    //endregion
}
