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

package com.welab.wefe.board.service.component.base.filter;

import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;

import java.util.Arrays;
import java.util.List;

/**
 * Query conditions: look for nodes of the specified output type
 *
 * @author zane.luo
 */
public class OutputDataTypesOutputFilter implements OutputItemFilterFunction {

    private final FlowGraph graph;
    private final List<IODataType> types;

    public OutputDataTypesOutputFilter(FlowGraph graph, IODataType... types) {
        this.graph = graph;
        this.types = Arrays.asList(types);
    }

    @Override
    public boolean apply(FlowGraphNode node, OutputItem outputItem) {

        return types.contains(outputItem.getDataType());
    }

}
