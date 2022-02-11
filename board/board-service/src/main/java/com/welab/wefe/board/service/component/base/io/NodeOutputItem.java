/**
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

package com.welab.wefe.board.service.component.base.io;

import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.wefe.enums.ComponentType;


/**
 * @author zane.luo
 */
public class NodeOutputItem extends OutputItem {
    private ComponentType componentType;
    private String nodeId;
    private String taskName;

    public NodeOutputItem() {
    }

    public NodeOutputItem(FlowGraphNode node, OutputItem item) {
        super.setName(item.getName());
        super.setDataType(item.getDataType());

        this.componentType = node.getComponentType();
        this.nodeId = node.getNodeId();
        this.taskName = node.getTaskName();
    }

    //region getter/setter

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    //endregion
}
