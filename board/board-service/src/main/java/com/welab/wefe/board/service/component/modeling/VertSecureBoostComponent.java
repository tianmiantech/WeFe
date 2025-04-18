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
import com.welab.wefe.board.service.component.base.filter.IntersectedOutputFilter;
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
public class VertSecureBoostComponent extends AbstractModelingComponent<VertSecureBoostComponent.Params> {
    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        FlowGraphNode intersectionNode = graph.findOneNodeFromParent(node, ComponentType.Intersection);
        if (intersectionNode == null) {
            throw new FlowNodeException(node, "请在前面添加样本对齐组件。");
        }
        
        List<JobMemberMySqlModel> jobMembers = graph.getMembers();
        long memberCount = jobMembers.size();
        if (memberCount > 2 && "layered".equalsIgnoreCase(params.otherParam.workMode)) {
            throw new FlowNodeException(node, "layered模式 只支持两个参与方");
        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.VertSecureBoost;
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

        JObject encryptParam = JObject.create()
                .append("method", params.encryptParam.method);

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
                .append("validation_freqs", params.otherParam.validationFreqs)
                .append("early_stopping_rounds", params.otherParam.earlyStoppingRounds)
                .append("tree_param", treeParam)
                .append("objective_param", objectiveParam)
                .append("encrypt_param", encryptParam)
                .append("cv_param", cvParam).append("work_mode", params.otherParam.workMode);
        if ("layered".equalsIgnoreCase(params.otherParam.workMode)) {
            output.append("promoter_depth", params.otherParam.promoterDepth).append("provider_depth",
                    params.otherParam.providerDepth);
        } else if ("skip".equalsIgnoreCase(params.otherParam.workMode)) {
            output.append("tree_num_per_member", params.otherParam.treeNumPerMember);
        } else if ("dp".equalsIgnoreCase(params.otherParam.workMode)) {
            output.append("epsilon", params.otherParam.epsilon);
        }

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
                InputMatcher.of(Names.Data.TRAIN_DATA_SET, new IntersectedOutputFilter(graph)),
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

    @Override
    protected boolean needIntersectedDataSetBeforeMe() {
        return true;
    }

    public static class Params extends AbstractSecureBoostInput {

        @Check(require = true)
        private EncryptParam encryptParam;
        
        

        public EncryptParam getEncryptParam() {
            return encryptParam;
        }

        public void setEncryptParam(EncryptParam encryptParam) {
            this.encryptParam = encryptParam;
        }

        public static class EncryptParam extends AbstractCheckModel {
            @Check(name = "同态加密算法", require = true)
            private String method;

            public String getMethod() {
                return method;
            }

            public void setMethod(String method) {
                this.method = method;
            }
        }

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
            @Check(name = "验证频率", require = true)
            private int validationFreqs;
            @Check(name = "允许提前结束的最小迭代次数", require = true)
            private int earlyStoppingRounds;
            @Check(name = "工作模式")
			private String workMode = "normal"; // normal、layered、skip
            
            // 当work_mode==layered时，需要下面两个参数
            @Check(name = "promoter层数")
            private int promoterDepth;
            @Check(name = "provider层数")
            private int providerDepth;
            
            // 当work_mode==skip时，需要下面这个参数
            @Check(name = "单方每次构建树的数量")
            private int treeNumPerMember;
            
            // 当work_mode==dp时 隐私预算
            @Check(name = "隐私预算") // 1.22
            private float epsilon;

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

            public int getValidationFreqs() {
                return validationFreqs;
            }

            public void setValidationFreqs(int validationFreqs) {
                this.validationFreqs = validationFreqs;
            }

            public int getEarlyStoppingRounds() {
                return earlyStoppingRounds;
            }

            public void setEarlyStoppingRounds(int earlyStoppingRounds) {
                this.earlyStoppingRounds = earlyStoppingRounds;
            }

			public String getWorkMode() {
				return workMode;
			}

			public void setWorkMode(String workMode) {
				this.workMode = workMode;
			}

			public int getPromoterDepth() {
				return promoterDepth;
			}

			public void setPromoterDepth(int promoterDepth) {
				this.promoterDepth = promoterDepth;
			}

			public int getProviderDepth() {
				return providerDepth;
			}

			public void setProviderDepth(int providerDepth) {
				this.providerDepth = providerDepth;
			}

			public int getTreeNumPerMember() {
				return treeNumPerMember;
			}

			public void setTreeNumPerMember(int treeNumPerMember) {
				this.treeNumPerMember = treeNumPerMember;
			}

            public float getEpsilon() {
                return epsilon;
            }

            public void setEpsilon(float epsilon) {
                this.epsilon = epsilon;
            }
        }

    }
}
