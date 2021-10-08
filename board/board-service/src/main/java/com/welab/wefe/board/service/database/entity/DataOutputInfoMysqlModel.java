/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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
 * @author aaron.li
 **/
@Entity(name = "data_output_info")
public class DataOutputInfoMysqlModel extends AbstractBaseMySqlModel {

    /**
     * 任务 Id
     */
    private String jobId;
    /**
     * 子任务 Id
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
     * 表空间
     */
    private String tableNamespace;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 创建时数量
     */
    private Integer tableCreateCount;
    /**
     * 当前数量
     */
    private Integer tableCurrentCount;
    /**
     * 分区数
     */
    private Integer partition;
    /**
     * 模型id
     */
    private String partyModelId;
    /**
     * 模型版本
     */
    private String modelVersion;
    /**
     * 描述
     */
    private String desc;

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

    public String getTableNamespace() {
        return tableNamespace;
    }

    public void setTableNamespace(String tableNamespace) {
        this.tableNamespace = tableNamespace;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getTableCreateCount() {
        return tableCreateCount;
    }

    public void setTableCreateCount(Integer tableCreateCount) {
        this.tableCreateCount = tableCreateCount;
    }

    public Integer getTableCurrentCount() {
        return tableCurrentCount;
    }

    public void setTableCurrentCount(Integer tableCurrentCount) {
        this.tableCurrentCount = tableCurrentCount;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public String getPartyModelId() {
        return partyModelId;
    }

    public void setPartyModelId(String partyModelId) {
        this.partyModelId = partyModelId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
