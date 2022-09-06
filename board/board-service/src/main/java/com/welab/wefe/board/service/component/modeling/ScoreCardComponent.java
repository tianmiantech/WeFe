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
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        FlowGraphNode intersectionNode3 = graph.findOneNodeFromParent(node, ComponentType.MixBinning);
        if (intersectionNode == null && intersectionNode2 == null && intersectionNode3 == null) {
            throw new FlowNodeException(node, "请在前面添加分箱组件。");
        }

        FlowGraphNode intersectionNode4 = graph.findOneNodeFromParent(node, ComponentType.HorzLR);
        FlowGraphNode intersectionNode5 = graph.findOneNodeFromParent(node, ComponentType.VertLR);
        FlowGraphNode intersectionNode6 = graph.findOneNodeFromParent(node, ComponentType.MixLR);
        if (intersectionNode4 == null && intersectionNode5 == null && intersectionNode6 == null) {
            throw new FlowNodeException(node, "请在前面添加逻辑回归组件。");
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

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndType(taskId, type);
        if (taskResult == null) {
            return null;
        }

        taskResult.setResult(getScoreCardResult(taskResult));

        return taskResult;
    }

    private String getScoreCardResult(TaskResultMySqlModel taskResult) {
        double bScore = extractBScore(taskResult);
        JObject binningResult = getBinningResult(taskResult);
        JObject modelResult = getModelResult(taskResult);

        JObject result = JObject.create();
        binningResult.entrySet().stream().forEach(x -> {
            double weight = modelResult.getDouble(x.getKey());
            List<Double> splitPoints = extractSplitPoints(JObject.create(x.getValue()));
            List<Double> woeArray = extractWoeArray(JObject.create(x.getValue()));

            List<Output> outputs = Lists.newArrayList();
            for (int i = 0; i < splitPoints.size(); i++) {
                Output output = new Output();
                output.setBinning(getBinningSplit(splitPoints, i));
                output.setWoe(woeArray.get(i));
                output.setScore(woeArray.get(i) * bScore * weight);
                output.setWeight(weight);
                outputs.add(output);
            }

            result.append(x.getKey(), outputs);
        });
        return result.toJSONString();
    }

    private List<Double> extractWoeArray(JObject obj) {
        return obj.getJSONList("woeArray", Double.class);
    }

    private List<Double> extractSplitPoints(JObject obj) {
        return obj.getJSONList("splitPoints", Double.class);
    }

    private JObject getBinningResult(TaskResultMySqlModel taskResult) {
        TaskResultMySqlModel binningTaskResult = taskResultService.findOne(
                taskResult.getJobId(),
                null,
                taskResult.getRole(),
                TaskResultType.model_binning.name()
        );

        JObject binning = JObject.create(binningTaskResult.getResult());
        return binning.getJObjectByPath("model_param.binningResult.binningResult");
    }

    private JObject getModelResult(TaskResultMySqlModel taskResult) {
        TaskResultMySqlModel binningTaskResult = taskResultService.findOne(
                taskResult.getJobId(),
                null,
                taskResult.getRole(),
                TaskResultType.model_train.name()
        );

        JObject binning = JObject.create(binningTaskResult.getResult());
        return binning.getJObjectByPath("model_param.weight");
    }

    public static String scoreCardKey(String name) {
        return "score_" + name;
    }

    private double extractBScore(TaskResultMySqlModel taskResult) {
        JObject scoreCard = JObject.create(taskResult.getResult());
        String jsonPath = scoreCardKey(taskResult.getName()) + ".data.b_score";
        return scoreCard.getDoubleByPath(jsonPath);
    }

    private String getBinningSplit(List<Double> list, int i) {
        String beforeKey = i == 0 ? "-∞" : precisionProcessByDouble(list.get(i - 1));
        return beforeKey + "," + precisionProcessByDouble(list.get(i));
    }

    private String precisionProcessByDouble(double value) {
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }


    private static class Output {
        private String binning;

        private Double woe;

        private Double score;

        private Double weight;

        public String getBinning() {
            return binning;
        }

        public void setBinning(String binning) {
            this.binning = binning;
        }

        public Double getWoe() {
            return woe;
        }

        public void setWoe(Double woe) {
            this.woe = woe;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }
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
