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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.enums.ComponentType;

/**
 * @author zane.luo
 */
public class ProjectFlowNodeOutputModel extends AbstractOutputModel {
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
     * 父节点
     */
    private String parentNodeIdList;
    /**
     * 组件类型
     */
    private ComponentType componentType;
    /**
     * 组件参数
     */
    private JSONObject params;
    /**
     * 参数版本号
     */
    private long paramsVersion;

    /**
     * 组件名称
     */
    public String getComponentName() {
        if (componentType == null) {
            return null;
        }
        return componentType.getLabel();
    }


    //region getter/setter

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

    public String getParentNodeIdList() {
        return parentNodeIdList;
    }

    public void setParentNodeIdList(String parentNodeIdList) {
        this.parentNodeIdList = parentNodeIdList;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(String params) {
        if (params != null) {
            this.params = JSONObject.parseObject(params);
        }
    }

    public long getParamsVersion() {
        return paramsVersion;
    }

    public void setParamsVersion(long paramsVersion) {
        this.paramsVersion = paramsVersion;
    }

    public boolean isStartNode() {
        return startNode;
    }

    public void setStartNode(boolean startNode) {
        this.startNode = startNode;
    }

    //endregion
}
