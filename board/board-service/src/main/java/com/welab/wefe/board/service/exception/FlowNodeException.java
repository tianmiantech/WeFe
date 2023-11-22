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

package com.welab.wefe.board.service.exception;

import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;

/**
 * @author zane.luo
 */
public class FlowNodeException extends StatusCodeWithException {
    private final FlowGraphNode node;


    public FlowNodeException(FlowGraphNode node, String message) {
        super(message, StatusCode.ERROR_IN_FLOW_GRAPH_NODE);
        this.node = node;
    }

    @Override
    public String getMessage() {
        if (node != null) {
            String message = "组件【" + node.getComponentType().getLabel() + "】中发生了异常：" + super.getMessage();

            if (node.getDeep() != null) {
                message = "位于深度 " + node.getDeep() + " 的" + message;
            }
            return message;
        } else {
            return super.getMessage();
        }
    }

    public FlowGraphNode getNode() {
        return node;
    }
}
