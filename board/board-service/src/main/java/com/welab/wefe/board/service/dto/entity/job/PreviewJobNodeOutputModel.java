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

package com.welab.wefe.board.service.dto.entity.job;


import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.ComponentType;

import java.util.Map;

/**
 * @author zane.luo
 */
public class PreviewJobNodeOutputModel {
    @Check(name = "前端画布中的节点id，由前端生成")
    private String nodeId;
    @Check(name = "项目ID")
    private String projectId;
    @Check(name = "父节点")
    private String parentNodeId;
    @Check(name = "组件类型")
    private ComponentType componentType;
    @Check(name = "深度")
    private Integer deep;
    @Check(name = "在任务列表中的序号，如果为 null，表示该节点不会被执行。")
    private Integer position;
    @Check(name = "是否存在可用的历史缓存结果")
    private Boolean hasCacheResult;
    public Map<String, Object> input;
    public Map<String, Object> output;

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

    public String getParentNodeId() {
        return parentNodeId;
    }

    public void setParentNodeId(String parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public Integer getDeep() {
        return deep;
    }

    public void setDeep(Integer deep) {
        this.deep = deep;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getHasCacheResult() {
        return hasCacheResult;
    }

    public void setHasCacheResult(Boolean hasCacheResult) {
        this.hasCacheResult = hasCacheResult;
    }

    //endregion
}
