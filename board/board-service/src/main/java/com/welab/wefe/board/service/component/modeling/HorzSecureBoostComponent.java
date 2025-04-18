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
import com.welab.wefe.common.web.dto.AbstractSecureBoostInput;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author lonnie
 */
@Service
public class HorzSecureBoostComponent extends AbstractModelingComponent<HorzSecureBoostComponent.Params> {
    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

    }


    @Override
    public ComponentType taskType() {
        return ComponentType.HorzSecureBoost;
    }

    @Override
    protected JSONObject createTaskParams(JobBuilder jobBuilder, FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        JObject output = JObject.create();
        JObject treeParam = JObject.create().append("criterion_method", "xgboost")
                .append("criterion_params", params.getTreeParam().getCriterionParams())
                .append("max_depth", params.getTreeParam().getMaxDepth())
                .append("min_sample_split", params.getTreeParam().getMinSampleSplit())
                .append("min_impurity_split", params.getTreeParam().getMinImpuritySplit())
                .append("min_leaf_node", params.getTreeParam().getMinLeafNode());

        JObject objectiveParam = JObject.create().append("objective", params.getObjectiveParam().getObjective())
                .append("params", params.getObjectiveParam().getParams());

        JObject cvParam = JObject.create()
                .append("n_splits", params.getCvParam().getnSplits())
                .append("shuffle", params.getCvParam().isShuffle())
                .append("need_cv", params.getCvParam().isNeedCv());


        output.append("task_type", params.otherParam.taskType)
                .append("learning_rate", params.otherParam.learningRate)
                .append("num_trees", params.otherParam.numTrees)
                .append("subsample_feature_rate", params.otherParam.subsampleFeatureRate)
                .append("n_iter_no_change", params.otherParam.nIterNoChange)
                .append("tol", params.otherParam.tol)
                .append("bin_num", params.otherParam.binNum)
                .append("tree_param", treeParam)
                .append("objective_param", objectiveParam)
                .append("cv_param", cvParam);

        output.append("grid_search_param",params.getGridSearchParam().toKernelParam());
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
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                InputMatcher.of(Names.Data.TRAIN_DATA_SET, IODataType.DataSetInstance),
                InputMatcher.of(Names.Data.EVALUATION_DATA_SET, TEST_DATA_SET_SUPPLIER)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance),
                OutputItem.of(Names.Model.TRAIN_MODEL, IODataType.ModelFromXGBoost)
        );
    }


    public static class Params extends AbstractSecureBoostInput {

        @Check(require = true)
        private OtherParam otherParam;

        public OtherParam getOtherParam() {
            return otherParam;
        }

        public void setOtherParam(OtherParam otherParam) {
            this.otherParam = otherParam;
        }

        public static class OtherParam extends AbstractCheckModel {
            @Check(name = "任务类型", require = true)
            private String taskType;
            @Check(name = "学习率", require = true)
            private float learningRate;
            @Check(name = "树数量", require = true)
            private int numTrees;
            @Check(name = "特征随机采样比率", require = true)
            private float subsampleFeatureRate;
            @Check(name = "多次迭代无变化是允许停止", require = true)
            private boolean nIterNoChange;
            @Check(name = "收敛阈值", require = true)
            private float tol;
            @Check(name = "最大分箱数", require = true)
            private int binNum;

            public String getTaskType() {
                return taskType;
            }

            public void setTaskType(String taskType) {
                this.taskType = taskType;
            }

            public float getLearningRate() {
                return learningRate;
            }

            public void setLearningRate(float learningRate) {
                this.learningRate = learningRate;
            }

            public int getNumTrees() {
                return numTrees;
            }

            public void setNumTrees(int numTrees) {
                this.numTrees = numTrees;
            }

            public float getSubsampleFeatureRate() {
                return subsampleFeatureRate;
            }

            public void setSubsampleFeatureRate(float subsampleFeatureRate) {
                this.subsampleFeatureRate = subsampleFeatureRate;
            }

            public boolean isnIterNoChange() {
                return nIterNoChange;
            }

            public void setnIterNoChange(boolean nIterNoChange) {
                this.nIterNoChange = nIterNoChange;
            }

            public float getTol() {
                return tol;
            }

            public void setTol(float tol) {
                this.tol = tol;
            }

            public int getBinNum() {
                return binNum;
            }

            public void setBinNum(int binNum) {
                this.binNum = binNum;
            }
        }

    }
}
