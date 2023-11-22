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

package com.welab.wefe.board.service.dto.entity;


import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import java.util.List;

/**
 * @author zane.luo
 */
public class ProjectMemberInput extends AbstractCheckModel {
    @Check(name = "成员Id", require = true, messageOnEmpty = "请选择项目合作方")
    private String memberId;
    @Check(name = "成员角色", donotShow = true)
    private JobMemberRole memberRole;

    @Check(name = "数据集列表")
    private List<ProjectDataSetInput> dataSetList;


    //region getter/setter

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public List<ProjectDataSetInput> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<ProjectDataSetInput> dataSetList) {
        this.dataSetList = dataSetList;
    }

    //endregion

}
