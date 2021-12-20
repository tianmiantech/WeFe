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

package com.welab.wefe.board.service.component.base.io;

import com.welab.wefe.board.service.component.base.filter.OutputDataTypesOutputFilter;
import com.welab.wefe.board.service.component.base.filter.OutputItemFilterFunction;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.wefe.enums.ComponentType;


/**
 * Component's input matcher
 *
 * @author zane.luo
 */
public class InputMatcher {
    /**
     * Need to match the name of the object
     */
    private String name;
    /**
     * Whether it is an optional parameter
     */
    private boolean optional;
    /**
     * Priority 1: Supply logic output input node directly through input parameter
     */
    private InputSupplier supplier;
    /**
     * Priority 2: Find the node that meets the conditions through the adaptation check logic
     */
    private OutputItemFilterFunction filter;
    /**
     * Priority 3: Automatic search according to the input parameter data type
     */
    private IODataType dataType;

    /**
     * Guess the input node of the specified node
     */
    public NodeOutputItem dopeOut(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {


        NodeOutputItem outputItem = null;

        // Strategy 1: First use supplier to try to find input nodes
        if (supplier != null) {
            outputItem = supplier.get(graph, node);
        }

        // Strategy 2: Try to use filter to find input nodes
        if (outputItem == null && filter != null) {
            outputItem = graph.findNodeOutputFromParent(node, filter);
        }

        // Strategy 3: Automatic search according to the required data type
        if (outputItem == null && dataType != null) {
            outputItem = graph.findNodeOutputFromParent(node, new OutputDataTypesOutputFilter(graph, dataType));
        }

        if (outputItem == null && !optional) {
            throw new FlowNodeException(node, buildMessageWhenDopeOutNone(name));
        }

        return outputItem;
    }

    /**
     * Generate a message when the front element cannot be found
     */
    private String buildMessageWhenDopeOutNone(String name) {
        switch (name) {
            case Names.Model.BINNING_MODEL:
                return "自动推测前置要素：" + ComponentType.Binning.getLabel() + " 未找到";
            case Names.Data.TRAIN_DATA_SET:
                return "自动推测前置要素：训练数据集 未找到，请检查流程是否合理。";
            case Names.Data.EVALUATION_DATA_SET:
                return "自动推测前置要素：验证数据集 未找到，请使用 “" + ComponentType.Segment.getLabel() + "” 生成验证集。";
            case Names.Data.NORMAL_DATA_SET:
                return "自动推测前置要素：数据集 未找到，请检查流程是否合理。";
            default:
                return "自动推测前置要素：" + name + " 未找到，请检查流程是否合理。";
        }
    }


    //region constructor

    public static InputMatcher of(String name, IODataType dataType) {
        InputMatcher matcher = new InputMatcher();
        matcher.name = name;
        matcher.dataType = dataType;
        return matcher;
    }

    public static InputMatcher of(String name, InputSupplier supplier) {
        InputMatcher matcher = new InputMatcher();
        matcher.name = name;
        matcher.supplier = supplier;
        return matcher;
    }

    public static InputMatcher of(String name, OutputItemFilterFunction filter) {
        InputMatcher matcher = new InputMatcher();
        matcher.name = name;
        matcher.filter = filter;
        return matcher;
    }

    //endregion

    //region getter/setter

    public String getName() {
        return name;
    }


    //endregion
}
