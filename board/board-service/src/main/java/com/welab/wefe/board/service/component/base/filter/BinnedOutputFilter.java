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

import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.wefe.enums.ComponentType;


/**
 * Query conditions: include data after binning
 *
 * @author zane.luo
 */
public class BinnedOutputFilter implements OutputItemFilterFunction {

    private final FlowGraph graph;

    public BinnedOutputFilter(FlowGraph graph) {
        this.graph = graph;
    }

    @Override
    public boolean apply(FlowGraphNode node, OutputItem outputItem) {
        return binned(graph, node, outputItem);
    }

    /**
     * Determine whether the data in a node is the data after binning
     */
    public static boolean binned(FlowGraph graph, FlowGraphNode node, OutputItem outputItem) {

        // The aligned data must be Instance
        if (outputItem.getDataType() != IODataType.DataSetInstance) {
            return false;
        }

        // The premise of binning is data alignment
        if (!IntersectedOutputFilter.intersected(graph, node, outputItem)) {
            return false;
        }

        // If the current node is binning, it is directly available
        if (node.getComponentType() == ComponentType.Binning) {
            return true;
        }

        // If the parent node of the node contains binning, it proves that the data is binned data.
        if (graph.findOneNodeFromParent(node, ComponentType.Binning) != null) {
            return true;
        }

        // If the original data source comes from the binned data, it proves that the data is binned data.
        FlowGraphNode dataIONode = graph.findOneNodeFromParent(node, ComponentType.DataIO);
        if (dataIONode != null) {
            DataIOComponent.Params params = (DataIOComponent.Params) dataIONode.getParamsModel();
            TableDataSetMysqlModel myDataSet = params.getMyDataSet();

            return myDataSet != null && myDataSet.getDerivedFrom() == ComponentType.Binning;

        }


        // None of the above
        return false;

    }
}
