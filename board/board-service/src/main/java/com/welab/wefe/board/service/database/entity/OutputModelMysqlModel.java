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

package com.welab.wefe.board.service.database.entity;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

import javax.persistence.Entity;

/**
 * 输出模型
 *
 * @author aaron.li
 **/
@Entity(name = "output_model")
public class OutputModelMysqlModel extends AbstractBaseMySqlModel {

    /**
     * 任务ID
     */
    private String jobId;
    /**
     * 子任务ID
     */
    private String taskId;
    /**
     * 组件名称
     */
    private String componentName;
    /**
     * 角色
     */
    private String role;
    /**
     * 成员id
     */
    private String memberId;
    /**
     * 模型id
     */
    private String memberModelId;
    /**
     * 模型版本
     */
    private String modelVersion;
    /**
     * 模型key
     */
    private String componentModelKey;
    /**
     * 模型信息
     */
    private String modelMeta;
    /**
     * 模型参数
     */
    private String modelParam;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberModelId() {
        return memberModelId;
    }

    public void setMemberModelId(String memberModelId) {
        this.memberModelId = memberModelId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getComponentModelKey() {
        return componentModelKey;
    }

    public void setComponentModelKey(String componentModelKey) {
        this.componentModelKey = componentModelKey;
    }

    public String getModelMeta() {
        return modelMeta;
    }

    public void setModelMeta(String modelMeta) {
        this.modelMeta = modelMeta;
    }

    public String getModelParam() {
        return modelParam;
    }

    public void setModelParam(String modelParam) {
        this.modelParam = modelParam;
    }
}
