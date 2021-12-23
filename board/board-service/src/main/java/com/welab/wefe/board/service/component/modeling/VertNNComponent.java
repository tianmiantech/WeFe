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
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class VertNNComponent extends AbstractModelingComponent<VertNNComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                        Params params) throws FlowNodeException {
        List<JobMemberMySqlModel> jobMembers = graph.getMembers();
        long providerCount = jobMembers.stream().filter(x -> x.getJobRole() == JobMemberRole.provider).count();
        if(providerCount == 2) {
            throw new FlowNodeException(node, "纵向深度学习不支持多个协作方，请保留一个协作方数据集");
        }
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                          Params params) throws FlowNodeException {
        JSONObject taskParam = new JSONObject();

        JObject vertNNParam = JObject.create();
        vertNNParam.append("epochs", params.epochs).append("interactive_layer_lr", params.interactiveLayerLr)
                .append("batch_size", params.batchSize).append("early_stop", "diff");

        JObject optimizer = JObject.create().append("learning_rate", params.learningRate).append("optimizer",
                params.optimizer);
        List<String> metrics = new ArrayList<>();
        metrics.add("AUC");
        vertNNParam.append("optimizer", optimizer).append("loss", params.loss).append("metrics", metrics);

        JObject bottomNNDefine = JObject.create().append("class_name", "Sequential").append("layers",
                params.bottomNNDefine.layers);

        vertNNParam.append("bottom_nn_define", bottomNNDefine);

        JObject interactiveLayerDefine = JObject.create().append("class_name", "Sequential").append("layers",
                params.interactiveLayerDefine.layers);

        vertNNParam.append("interactive_layer_define", interactiveLayerDefine);

        JObject topNNDefine = JObject.create().append("class_name", "Sequential").append("layers",
                params.topNNDefine.layers);

        vertNNParam.append("top_nn_define", topNNDefine);

        vertNNParam.append("config_type", "keras");

        taskParam.put("params", vertNNParam);

        return taskParam;
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.VertNN;
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
        private int epochs;

        @Check(name = "交互层学习率", require = true)
        private float interactiveLayerLr;

        @Check(name = "批量大小", require = true)
        private int batchSize;

        @Check(name = "学习率", require = true)
        private float learningRate;

        @Check(name = "优化器", require = true)
        private String optimizer;

        @Check(name = "损失函数", require = true)
        private String loss;

        @Check(name = "底层", require = true)
        private BottomNNDefine bottomNNDefine;

        @Check(name = "中间交互层", require = true)
        private InteractiveLayerDefine interactiveLayerDefine;

        @Check(name = "顶层", require = true)
        private TopNNDefine topNNDefine;

        public static class BottomNNDefine extends AbstractCheckModel {
            private List<Layer> layers;

            public List<Layer> getLayers() {
                return layers;
            }

            public void setLayers(List<Layer> layers) {
                this.layers = layers;
            }
        }

        public static class InteractiveLayerDefine extends AbstractCheckModel {
            private List<Layer> layers;

            public List<Layer> getLayers() {
                return layers;
            }

            public void setLayers(List<Layer> layers) {
                this.layers = layers;
            }
        }

        public static class TopNNDefine extends AbstractCheckModel {
            private List<Layer> layers;

            public List<Layer> getLayers() {
                return layers;
            }

            public void setLayers(List<Layer> layers) {
                this.layers = layers;
            }
        }

        public int getEpochs() {
            return epochs;
        }

        public void setEpochs(int epochs) {
            this.epochs = epochs;
        }

        public float getInteractiveLayerLr() {
            return interactiveLayerLr;
        }

        public void setInteractiveLayerLr(float interactiveLayerLr) {
            this.interactiveLayerLr = interactiveLayerLr;
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

        public BottomNNDefine getBottomNNDefine() {
            return bottomNNDefine;
        }

        public void setBottomNNDefine(BottomNNDefine bottomNNDefine) {
            this.bottomNNDefine = bottomNNDefine;
        }

        public InteractiveLayerDefine getInteractiveLayerDefine() {
            return interactiveLayerDefine;
        }

        public void setInteractiveLayerDefine(InteractiveLayerDefine interactiveLayerDefine) {
            this.interactiveLayerDefine = interactiveLayerDefine;
        }

        public TopNNDefine getTopNNDefine() {
            return topNNDefine;
        }

        public void setTopNNDefine(TopNNDefine topNNDefine) {
            this.topNNDefine = topNNDefine;
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
