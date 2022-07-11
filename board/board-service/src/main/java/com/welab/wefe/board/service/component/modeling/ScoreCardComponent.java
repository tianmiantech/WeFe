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

package com.welab.wefe.board.service.component.modeling;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.model.JobBuilder;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author hunter
 */
@Service
public class ScoreCardComponent extends AbstractModelingComponent<ScoreCardComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        FlowGraphNode intersectionNode = graph.findOneNodeFromParent(node, ComponentType.Binning);
        FlowGraphNode intersectionNode2 = graph.findOneNodeFromParent(node, ComponentType.HorzFeatureBinning);
        if (intersectionNode == null && intersectionNode2==null) {
            throw new FlowNodeException(node, "请在前面添加分箱组件。");
        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.ScoreCard;
    }

    @Override
    protected JSONObject createTaskParams(JobBuilder jobBuilder, FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        JObject output = JObject.create();
        output.append("p0", params.getP0())
                .append("pdo", params.getPdo());

        return output;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        return super.getResult(taskId, type);
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode flowGraphNode) {
        return Arrays.asList(
                InputMatcher.of(Names.Model.BINNING_MODEL, IODataType.ModelFromBinning),
                InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(OutputItem.of(Names.JSON_RESULT, IODataType.Json));
    }

    public static class Params extends AbstractCheckModel {
        @Check(name = "基准分", require = true)
        private double p0;

        @Check(name = "pdo", require = true)
        private double pdo;

        public double getP0() {
            return p0;
        }

        public void setP0(double p0) {
            this.p0 = p0;
        }

        public double getPdo() {
            return pdo;
        }

        public void setPdo(double pdo) {
            this.pdo = pdo;
        }
    }
}
