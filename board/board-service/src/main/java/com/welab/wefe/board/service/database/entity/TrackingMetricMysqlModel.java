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

import com.welab.wefe.board.service.database.entity.base.AbstractMySqlModel;

import javax.persistence.Entity;

/**
 * 跟踪指标
 *
 * @author aaron.li
 **/
@Entity(name = "tracking_metric")
public class TrackingMetricMysqlModel extends AbstractMySqlModel {

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
     * metric命名空间
     */
    private String metricNamespace;
    /**
     * metric名称
     */
    private String metricName;
    /**
     * metric类型
     */
    private String metricType;
    /**
     * 曲线名称
     */
    private String curveName;
    /**
     * 横坐标名称
     */
    private String abscissaName;
    /**
     * 纵坐标名称
     */
    private String ordinateName;
    private String pairType;
    /**
     * key
     */
    private String key;
    /**
     * 值
     */
    private String value;

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

    public String getMetricNamespace() {
        return metricNamespace;
    }

    public void setMetricNamespace(String metricNamespace) {
        this.metricNamespace = metricNamespace;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getCurveName() {
        return curveName;
    }

    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    public String getAbscissaName() {
        return abscissaName;
    }

    public void setAbscissaName(String abscissaName) {
        this.abscissaName = abscissaName;
    }

    public String getOrdinateName() {
        return ordinateName;
    }

    public void setOrdinateName(String ordinateName) {
        this.ordinateName = ordinateName;
    }

    public String getPairType() {
        return pairType;
    }

    public void setPairType(String pairType) {
        this.pairType = pairType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
