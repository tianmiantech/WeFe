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

package com.welab.wefe.board.service.component.base.filter;

import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.enums.ComponentType;

/**
 * Query condition: aligned data
 *
 * @author zane.luo
 */
public class IntersectedOutputFilter implements OutputItemFilterFunction {

    private final FlowGraph graph;

    public IntersectedOutputFilter(FlowGraph graph) {
        this.graph = graph;
    }

    @Override
    public boolean apply(FlowGraphNode node, OutputItem outputItem) {


        return intersected(graph, node, outputItem);
    }

    public static boolean intersected(FlowGraph graph, FlowGraphNode node, OutputItem outputItem) {

        // The aligned data must be Instance
        if (outputItem.getDataType() != IODataType.DataSetInstance) {
            return false;
        }

        // If the current node is aligned, it is directly available.
        if (node.getComponentType() == ComponentType.Intersection) {
            return true;
        }

        // If the parent node of the node contains alignment, it proves that the data is aligned data.
        if (graph.findOneNodeFromParent(node, ComponentType.Intersection) != null) {
            return true;
        }

        // If the original data source comes from aligned data, it proves that this data is aligned data.
        FlowGraphNode dataIONode = graph.findOneNodeFromParent(node, ComponentType.DataIO);
        if (dataIONode != null) {
            DataIOComponent.Params params = (DataIOComponent.Params) dataIONode.getParamsModel();
            TableDataSetMysqlModel myDataSet = params.getMyDataSet();

            // If it is not a derived data set, it must have been misaligned.
            if (myDataSet == null || !myDataSet.isDerivedResource()) {
                return false;
            }


            // If the derived data set comes from alignment
            if (myDataSet.getDerivedFrom() == ComponentType.Intersection) {
                return true;
            }

            /**
             * Here is a bit rough for the time being,
             * thinking that all the derived data sets are aligned.
             *
             * If you don't do this,
             * you need to investigate the source job of the derived data set and search recursively.
             */
            return true;

        }

        // None of the above
        return false;
    }
}
