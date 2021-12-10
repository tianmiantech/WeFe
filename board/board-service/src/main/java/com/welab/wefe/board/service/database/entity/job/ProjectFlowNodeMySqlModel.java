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

package com.welab.wefe.board.service.database.entity.job;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.enums.ComponentType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 */
@Entity(name = "project_flow_node")
public class ProjectFlowNodeMySqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = 2722275392448819712L;

    /**
     * 是否是起始节点
     */
    private boolean startNode;
    /**
     * 前端画布中的节点id，由前端生成
     */
    private String nodeId;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 流程ID
     */
    private String flowId;
    /**
     * 逗号分隔的父节点Id列表
     */
    private String parentNodeIdList;
    /**
     * 组件类型
     */
    @Enumerated(EnumType.STRING)
    private ComponentType componentType;
    /**
     * 组件参数
     */
    private String params;
    /**
     * 参数版本号（时间戳）
     */
    private long paramsVersion = System.currentTimeMillis();


    //region getter/setter


    public boolean isStartNode() {
        return startNode;
    }

    public void setStartNode(boolean startNode) {
        this.startNode = startNode;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getParentNodeIdList() {
        return parentNodeIdList;
    }

    public void setParentNodeIdList(String parentNodeId) {
        this.parentNodeIdList = parentNodeId;
    }

    public long getParamsVersion() {
        return paramsVersion;
    }

    public void setParamsVersion(long paramsVersion) {
        this.paramsVersion = paramsVersion;
    }

    //endregion
}
