/**
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

package com.welab.wefe.board.service.dto.entity.job;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.JobMemberRole;

/**
 * @author seven.zeng
 */
public class JobMemberOutputModel extends AbstractOutputModel {
    /**
     * 项目Id
     */
    private String projectId;
    /**
     * 流程Id
     */
    private String flowId;
    /**
     * 任务Id
     */
    private String jobId;
    /**
     * 在任务中的角色 枚举（promoter/provider/arbiter）
     */
    private JobMemberRole jobRole;
    /**
     * 成员 Id
     */
    private String memberId;
    /**
     * 数据集 Id
     */
    private String dataSetId;

    public String getMemberName() {
        return CacheObjects.getMemberName(memberId);
    }


    //region getter/setter

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobMemberRole getJobRole() {
        return jobRole;
    }

    public void setJobRole(JobMemberRole jobRole) {
        this.jobRole = jobRole;
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


    //endregion
}
