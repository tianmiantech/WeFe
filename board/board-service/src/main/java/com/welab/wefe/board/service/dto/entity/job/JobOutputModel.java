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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.JobStatus;

import java.util.Date;

/**
 * @author zane.luo
 */
public class JobOutputModel extends AbstractOutputModel {
    /**
     * 联邦任务类型（横向/纵向）
     */
    private FederatedLearningType federatedLearningType;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 流程ID
     */
    private String flowId;
    /**
     * 任务ID
     */
    private String jobId;
    /**
     * 名称
     */
    private String name;
    /**
     * 我方身份 枚举（promoter/provider/arbiter）
     */
    private JobMemberRole myRole;
    /**
     * 状态 枚举
     */
    private JobStatus status;
    /**
     * 状态更新时间
     */
    private Date statusUpdatedTime;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date finishTime;
    /**
     * 进度
     */
    private Integer progress;
    /**
     * 进度更新时间
     */
    private Date progressUpdatedTime;
    /**
     * 消息备注 失败原因/备注
     */
    private String message;
    /**
     * 有向无环图
     */
    private JSONObject graph;
    /**
     * 是否包含建模结果
     */
    private Boolean hasModelingResult;
    /**
     * 收藏/置顶/标记
     */
    private Boolean star;
    /**
     * 备注
     */
    private String remark;

    public JSONObject getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        // 将字符串序列化为 json 后再输出
        if (graph != null) {
            this.graph = JSON.parseObject(graph);
        }
    }

    //region getter/setter


    public FederatedLearningType getFederatedLearningType() {
        return federatedLearningType;
    }

    public void setFederatedLearningType(FederatedLearningType federatedLearningType) {
        this.federatedLearningType = federatedLearningType;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JobMemberRole getMyRole() {
        return myRole;
    }

    public void setMyRole(JobMemberRole myRole) {
        this.myRole = myRole;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Date getStatusUpdatedTime() {
        return statusUpdatedTime;
    }

    public void setStatusUpdatedTime(Date statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Date getProgressUpdatedTime() {
        return progressUpdatedTime;
    }

    public void setProgressUpdatedTime(Date progressUpdatedTime) {
        this.progressUpdatedTime = progressUpdatedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getHasModelingResult() {
        return hasModelingResult;
    }

    public void setHasModelingResult(Boolean hasModelingResult) {
        this.hasModelingResult = hasModelingResult;
    }

    public Boolean getStar() {
        return star;
    }

    public void setStar(Boolean star) {
        this.star = star;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    //endregion
}
