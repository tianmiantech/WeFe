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

package com.welab.wefe.board.service.component;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.enums.TaskResultType;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Segment component
 *
 * @author lonnie
 */
@Service
class SegmentComponent extends AbstractComponent<SegmentComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        double trainingRatio = params.getTrainingRatio();
        double verificationRatio = params.getVerificationRatio();
        if (trainingRatio <= 0 || trainingRatio >= 100 || verificationRatio <= 0 || verificationRatio >= 100) {
            throw new FlowNodeException(node, "训练与验证数据的比例需要在0到100之间。");
        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.Segment;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        // Reassemble front-end parameters
        JObject output = JObject.create();

        FederatedLearningType federatedLearningType = graph.getJob().getFederatedLearningType();
        if (federatedLearningType == FederatedLearningType.vertical) {
            output.append("mode", "vert");
        } else if (federatedLearningType == FederatedLearningType.horizontal) {
            output.append("mode", "horz");
        }

        // Take with_label from the task parameter of the dataIO component
        FlowGraphNode dataIONode = graph.findOneNodeFromParent(node, ComponentType.DataIO);
        TaskMySqlModel dataIOTask = findTaskFromPretasks(preTasks, dataIONode);

        if (dataIONode == null || dataIOTask == null) {
            throw new FlowNodeException(node, "请添加DataIO组件!");
        }

        JObject taskConfig = JObject.create(dataIOTask.getTaskConf());
        boolean withLabel = taskConfig.getBooleanValue("with_label");

        output.append("random_num", params.getSplitDataRandomNum())
                .append("train_ratio", params.getTrainingRatio() / (params.getTrainingRatio() + params.getVerificationRatio()))
                .append("with_label", withLabel)
                .append("label_name", "y")
                .append("label_type", "int");

        return output;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        TaskResultMySqlModel resultModel = taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_train_eval.name());
        if (resultModel == null) {
            return null;
        }
        JObject resultObj = JObject.create(resultModel.getResult());

        boolean withLabel = Boolean.parseBoolean(resultObj.getStringByPath("train_eval_segment.data.with_label.value"));
        int trainCount = resultObj.getIntegerByPath("train_eval_segment.data.train_count.value", 0);
        // Number of positive training examples
        int trainyPositiveExampleCount = resultObj.getIntegerByPath("train_eval_segment.data.train_y_positive_example_count.value", 0);

        // Proportion of training positive examples
        double trainyPositiveExampleRatio = resultObj.getDoubleByPath("train_eval_segment.data.train_y_positive_example_ratio.value", 0d);
        int evalCount = resultObj.getIntegerByPath("train_eval_segment.data.eval_count.value", 0);

        // Verify the number of positive examples
        int evalyPositiveExampleCount = resultObj.getIntegerByPath("train_eval_segment.data.eval_y_positive_example_count.value", 0);

        // Verify the proportion of positive cases
        double evalyPositiveExampleVatio = resultObj.getDoubleByPath("train_eval_segment.data.eval_y_positive_example_ratio.value", 0d);

        resultModel.setResult(JObject.create()
                .append("contains_y", withLabel)
                .append("train_count", trainCount)
                .append("train_y_positive_example_count", trainyPositiveExampleCount)
                .append("train_y_positive_example_ratio", trainyPositiveExampleRatio)
                .append("eval_count", evalCount)
                .append("eval_y_positive_example_count", evalyPositiveExampleCount)
                .append("eval_y_positive_example_ratio", evalyPositiveExampleVatio).toJSONString());

        return resultModel;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {

        return Arrays.asList(
                InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.Data.TRAIN_DATA_SET, IODataType.DataSetInstance),
                OutputItem.of(Names.Data.EVALUATION_DATA_SET, IODataType.DataSetInstance)
        );
    }

    public static class Params extends AbstractCheckModel {

        @Check(name = "切分随机数", require = true)
        private int splitDataRandomNum;

        @Check(name = "训练集", require = true)
        private double trainingRatio;

        @Check(name = "验证集", require = true)
        private double verificationRatio;

        public int getSplitDataRandomNum() {
            return splitDataRandomNum;
        }

        public void setSplitDataRandomNum(int splitDataRandomNum) {
            this.splitDataRandomNum = splitDataRandomNum;
        }

        public double getTrainingRatio() {
            return trainingRatio;
        }

        public void setTrainingRatio(double trainingRatio) {
            this.trainingRatio = trainingRatio;
        }

        public double getVerificationRatio() {
            return verificationRatio;
        }

        public void setVerificationRatio(double verificationRatio) {
            this.verificationRatio = verificationRatio;
        }
    }
}
