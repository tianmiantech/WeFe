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
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.dto.AbstractLRInput;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author lonnie
 */
@Service
public class HorzLRComponent extends AbstractModelingComponent<HorzLRComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

    }


    @Override
    public ComponentType taskType() {
        return ComponentType.HorzLR;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        JObject output = JObject.create();
        output.append("penalty", params.otherParam.penalty)
                .append("tol", params.otherParam.tol)
                .append("alpha", params.otherParam.alpha)
                .append("optimizer", params.otherParam.optimizer)
                .append("batch_size", params.otherParam.batchSize)
                .append("learning_rate", params.otherParam.learningRate)
                .append("max_iter", params.otherParam.maxIter)
                .append("early_stop", params.otherParam.earlyStop)
                .append("decay", params.otherParam.decay)
                .append("decay_sqrt", params.otherParam.decaySqrt)
                .append("multi_class", params.otherParam.multiClass)
                .append("init_method", params.getInitParam().getInitMethod())
                .append("fit_intercept", params.getInitParam().getFitIntercept())
                .append("n_splits", params.getCvParam().getnSplits())
                .append("shuffle", params.getCvParam().isShuffle())
                .append("need_cv", params.getCvParam().isNeedCv());

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
                InputMatcher.of(Names.Data.TRAIN_DATA_SET, TRAIN_DATA_SET_FILTER),
                InputMatcher.of(Names.Data.EVALUATION_DATA_SET, TEST_DATA_SET_SUPPLIER)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance),
                OutputItem.of(Names.Model.TRAIN_MODEL, IODataType.ModelFromLr)
        );
    }

    public static class Params extends AbstractLRInput {
        @Check(require = true)
        private OtherParam otherParam;

        public OtherParam getOtherParam() {
            return otherParam;
        }

        public void setOtherParam(OtherParam otherParam) {
            this.otherParam = otherParam;
        }

        public static class OtherParam extends AbstractCheckModel {
            @Check(name = "惩罚方式", require = true)
            private String penalty;

            @Check(name = "收敛容忍度", require = true)
            private float tol;

            @Check(name = "惩罚项系数", require = true)
            private float alpha;

            @Check(name = "优化算法", require = true)
            private String optimizer;

            @Check(name = "批量大小", require = true)
            private int batchSize;

            @Check(name = "学习率", require = true)
            private float learningRate;

            @Check(name = "最大迭代次数", require = true)
            private int maxIter;

            @Check(name = "判断收敛与否的方法", require = true)
            private String earlyStop;

            @Check(name = "学习速率的衰减率", require = true)
            private float decay;

            @Check(name = "衰减率是否开平方", require = true)
            private boolean decaySqrt;

            @Check(name = "多分类策略", require = true)
            private String multiClass;

            public String getPenalty() {
                return penalty;
            }

            public void setPenalty(String penalty) {
                this.penalty = penalty;
            }

            public float getTol() {
                return tol;
            }

            public void setTol(float tol) {
                this.tol = tol;
            }

            public float getAlpha() {
                return alpha;
            }

            public void setAlpha(float alpha) {
                this.alpha = alpha;
            }

            public String getOptimizer() {
                return optimizer;
            }

            public void setOptimizer(String optimizer) {
                this.optimizer = optimizer;
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

            public int getMaxIter() {
                return maxIter;
            }

            public void setMaxIter(int maxIter) {
                this.maxIter = maxIter;
            }

            public String getEarlyStop() {
                return earlyStop;
            }

            public void setEarlyStop(String earlyStop) {
                this.earlyStop = earlyStop;
            }

            public float getDecay() {
                return decay;
            }

            public void setDecay(float decay) {
                this.decay = decay;
            }

            public boolean isDecaySqrt() {
                return decaySqrt;
            }

            public void setDecaySqrt(boolean decaySqrt) {
                this.decaySqrt = decaySqrt;
            }

            public String getMultiClass() {
                return multiClass;
            }

            public void setMultiClass(String multiClass) {
                this.multiClass = multiClass;
            }
        }
    }
}
