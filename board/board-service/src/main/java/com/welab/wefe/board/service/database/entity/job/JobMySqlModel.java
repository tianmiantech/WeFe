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

package com.welab.wefe.board.service.database.entity.job;

import com.alibaba.fastjson.JSONObject;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.board.service.dto.kernel.machine_learning.KernelJob;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.JobStatus;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author Zane
 */
@Entity(name = "job")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class JobMySqlModel extends AbstractBaseMySqlModel {
    private static final long serialVersionUID = 8933206598650203308L;
    /**
     * 联邦任务类型（横向/纵向）
     */
    @Enumerated(EnumType.STRING)
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
    @Enumerated(EnumType.STRING)
    private JobMemberRole myRole;
    /**
     * 状态 枚举
     */
    @Enumerated(EnumType.STRING)
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
    private String graph;
    /**
     * 是否包含建模结果
     */
    private boolean hasModelingResult;
    /**
     * 收藏/置顶/标记
     */
    private boolean star;
    /**
     * 备注
     */
    private String remark;
    /**
     * job配置信息
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject jobConfig;


    public void setJobConfig(KernelJob kernelJob) {
        this.jobConfig = kernelJob.toJson();
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

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public boolean isHasModelingResult() {
        return hasModelingResult;
    }

    public void setHasModelingResult(boolean hasModelingResult) {
        this.hasModelingResult = hasModelingResult;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public JSONObject getJobConfig() {
        return jobConfig;
    }

    public void setJobConfig(JSONObject jobConfig) {
        this.jobConfig = jobConfig;
    }

    //endregion
}
