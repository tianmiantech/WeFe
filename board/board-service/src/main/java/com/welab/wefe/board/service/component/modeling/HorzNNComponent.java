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

package com.welab.wefe.board.service.component.modeling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;

@Service
public class HorzNNComponent extends AbstractModelingComponent<HorzNNComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                        Params params) throws FlowNodeException {
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                          Params params) throws FlowNodeException {
        JSONObject taskParam = new JSONObject();
        JObject horzNNParam = JObject.create();
        horzNNParam.append("encode_label", false).append("max_iter", params.maxIter).append("batch_size",
                params.batchSize);

        JObject earlyStop = JObject.create("early_stop", "diff").append("eps", 0.0);
        horzNNParam.append("early_stop", earlyStop);

        JObject optimizer = JObject.create().append("learning_rate", params.learningRate).append("decay", params.decay)
                .append("beta_1", 0.9).append("beta_2", 0.999).append("epsilon", 1e-07).append("amsgrad", false)
                .append("optimizer", params.optimizer);
        List<String> metrics = new ArrayList<>();
        metrics.add("AUC");
        metrics.add("Hinge");
        metrics.add("accuracy");
        horzNNParam.append("optimizer", optimizer).append("loss", params.loss).append("metrics", metrics);

        JObject nnDefine = JObject.create().append("class_name", "Sequential").append("layers", params.nnDefine.layers);
        horzNNParam.append("nn_define", nnDefine).append("config_type", "keras");

        taskParam.put("params", horzNNParam);
        return taskParam;
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.HorzNN;
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
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(InputMatcher.of(Names.Data.TRAIN_DATA_SET, TRAIN_DATA_SET_FILTER),
                InputMatcher.of(Names.Data.EVALUATION_DATA_SET, TEST_DATA_SET_SUPPLIER));
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance),
                OutputItem.of(Names.Model.TRAIN_MODEL, IODataType.ModelFromNN));
    }

    public static class Params extends AbstractCheckModel {
        @Check(name = "最大迭代次数", require = true)
        private int maxIter;
        @Check(name = "批量大小", require = true)
        private int batchSize;
        @Check(name = "学习率", require = true)
        private float learningRate;
        @Check(name = "学习率衰减值", require = true)
        private float decay;
        @Check(name = "优化器", require = true)
        private String optimizer;
        @Check(name = "损失函数", require = true)
        private String loss;
        @Check(name = "每层参数", require = true)
        private NNDefine nnDefine;

        public int getMaxIter() {
            return maxIter;
        }

        public void setMaxIter(int maxIter) {
            this.maxIter = maxIter;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public float getLearningRate() {
            return learningRate;
        }

        public void setLearningRate(float learningRate) {
            this.learningRate = learningRate;
        }

        public float getDecay() {
            return decay;
        }

        public void setDecay(float decay) {
            this.decay = decay;
        }

        public String getOptimizer() {
            return optimizer;
        }

        public void setOptimizer(String optimizer) {
            this.optimizer = optimizer;
        }

        public String getLoss() {
            return loss;
        }

        public void setLoss(String loss) {
            this.loss = loss;
        }

        public NNDefine getNnDefine() {
            return nnDefine;
        }

        public void setNnDefine(NNDefine nnDefine) {
            this.nnDefine = nnDefine;
        }

        public static class NNDefine extends AbstractCheckModel {
            private List<Layer> layers;

            public List<Layer> getLayers() {
                return layers;
            }

            public void setLayers(List<Layer> layers) {
                this.layers = layers;
            }
        }

        public static class Layer extends AbstractCheckModel {
            @Check(name = "定义", require = true)
            private String className;
            @Check(name = "配置", require = true)
            private LayerConfig config;

            public LayerConfig getConfig() {
                return config;
            }

            public void setConfig(LayerConfig config) {
                this.config = config;
            }

            public String getClassName() {
                return className;
            }

            public void setClassName(String className) {
                this.className = className;
            }
        }

        public static class LayerConfig extends AbstractCheckModel {
            @Check(name = "输出维度", require = true)
            private int units;
            @Check(name = "输入维度", require = false)
            private List<Integer> inputShape;
            @Check(name = "激活函数", require = true)
            private String activation;

            public int getUnits() {
                return units;
            }

            public void setUnits(int units) {
                this.units = units;
            }

            public List<Integer> getInputShape() {
                return inputShape;
            }

            public void setInputShape(List<Integer> inputShape) {
                this.inputShape = inputShape;
            }

            public String getActivation() {
                return activation;
            }

            public void setActivation(String activation) {
                this.activation = activation;
            }

        }
    }

}
