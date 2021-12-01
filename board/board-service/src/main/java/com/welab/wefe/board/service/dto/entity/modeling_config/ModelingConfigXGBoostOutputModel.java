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

package com.welab.wefe.board.service.dto.entity.modeling_config;

import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * 模型配置·逻辑回归
 *
 * @author Zane
 */
public class ModelingConfigXGBoostOutputModel extends AbstractModelingConfigOutputModel {
    @Check(name = "任务类型;枚举（分类/回归）")
    private String taskType;
    @Check(name = "学习率")
    private Double learningRate;
    @Check(name = "最大树数量")
    private Integer numTrees;
    @Check(name = "特征随机采样比率")
    private Double subsampleFeatureRate;
    @Check(name = "n次迭代没变化是否停止")
    private Boolean nIterNoChange;
    @Check(name = "收敛阀值")
    private Double tol;
    @Check(name = "最大桶数量")
    private Integer binNum;
    @Check(name = "标准函数;默认xgboost")
    private String treeParam_CriterionMethod;
    @Check(name = "标准参数")
    private String treeParam_CriterionParams;
    @Check(name = "树的最大深度")
    private Integer treeParam_MaxDepth;
    @Check(name = "分裂一个内部节点(非叶子节点)需要的最小样本;默认2")
    private Integer treeParam_MinSampleSplit;
    @Check(name = "每个叶子节点包含的最小样本数")
    private Integer treeParam_MinLeafNode;
    @Check(name = "单个拆分的要达到的最小增益")
    private Double treeParam_MinImpuritySplit;
    @Check(name = "可拆分的最大并行数量")
    private Integer treeParam_MaxSplitNodes;
    @Check(name = "目标函数")
    private String objectiveParam_Objective;
    @Check(name = "学习目标参数")
    private String objectiveParam_Params;
    @Check(name = "加密算法")
    private String encryptParam_Method;

    @Check(name = "在KFold中使用分割符大小")
    private Integer cvParam_NSplits;
    @Check(name = "在KFold之前是否进行洗牌")
    private Boolean cvParam_Shuffle;
    @Check(name = "是否需要进行此模块")
    private Boolean cvParam_NeedCv;
    @Check(name = "验证频次")
    private Integer validationFreqs;
    @Check(name = "提前结束的迭代次数")
    private Integer earlyStoppingRounds;

    //region getter/setter

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(Double learningRate) {
        this.learningRate = learningRate;
    }

    public Integer getNumTrees() {
        return numTrees;
    }

    public void setNumTrees(Integer numTrees) {
        this.numTrees = numTrees;
    }

    public Double getSubsampleFeatureRate() {
        return subsampleFeatureRate;
    }

    public void setSubsampleFeatureRate(Double subsampleFeatureRate) {
        this.subsampleFeatureRate = subsampleFeatureRate;
    }

    public Boolean getnIterNoChange() {
        return nIterNoChange;
    }

    public void setnIterNoChange(Boolean nIterNoChange) {
        this.nIterNoChange = nIterNoChange;
    }

    public Double getTol() {
        return tol;
    }

    public void setTol(Double tol) {
        this.tol = tol;
    }

    public Integer getBinNum() {
        return binNum;
    }

    public void setBinNum(Integer binNum) {
        this.binNum = binNum;
    }

    public String getTreeParam_CriterionMethod() {
        return treeParam_CriterionMethod;
    }

    public void setTreeParam_CriterionMethod(String treeParam_CriterionMethod) {
        this.treeParam_CriterionMethod = treeParam_CriterionMethod;
    }

    public String getTreeParam_CriterionParams() {
        return treeParam_CriterionParams;
    }

    public void setTreeParam_CriterionParams(String treeParam_CriterionParams) {
        this.treeParam_CriterionParams = treeParam_CriterionParams;
    }

    public Integer getTreeParam_MaxDepth() {
        return treeParam_MaxDepth;
    }

    public void setTreeParam_MaxDepth(Integer treeParam_MaxDepth) {
        this.treeParam_MaxDepth = treeParam_MaxDepth;
    }

    public Integer getTreeParam_MinSampleSplit() {
        return treeParam_MinSampleSplit;
    }

    public void setTreeParam_MinSampleSplit(Integer treeParam_MinSampleSplit) {
        this.treeParam_MinSampleSplit = treeParam_MinSampleSplit;
    }

    public Integer getTreeParam_MinLeafNode() {
        return treeParam_MinLeafNode;
    }

    public void setTreeParam_MinLeafNode(Integer treeParam_MinLeafNode) {
        this.treeParam_MinLeafNode = treeParam_MinLeafNode;
    }

    public Double getTreeParam_MinImpuritySplit() {
        return treeParam_MinImpuritySplit;
    }

    public void setTreeParam_MinImpuritySplit(Double treeParam_MinImpuritySplit) {
        this.treeParam_MinImpuritySplit = treeParam_MinImpuritySplit;
    }

    public Integer getTreeParam_MaxSplitNodes() {
        return treeParam_MaxSplitNodes;
    }

    public void setTreeParam_MaxSplitNodes(Integer treeParam_MaxSplitNodes) {
        this.treeParam_MaxSplitNodes = treeParam_MaxSplitNodes;
    }

    public String getObjectiveParam_Objective() {
        return objectiveParam_Objective;
    }

    public void setObjectiveParam_Objective(String objectiveParam_Objective) {
        this.objectiveParam_Objective = objectiveParam_Objective;
    }

    public String getObjectiveParam_Params() {
        return objectiveParam_Params;
    }

    public void setObjectiveParam_Params(String objectiveParam_Params) {
        this.objectiveParam_Params = objectiveParam_Params;
    }

    public String getEncryptParam_Method() {
        return encryptParam_Method;
    }

    public void setEncryptParam_Method(String encryptParam_Method) {
        this.encryptParam_Method = encryptParam_Method;
    }

    public Integer getCvParam_NSplits() {
        return cvParam_NSplits;
    }

    public void setCvParam_NSplits(Integer cvParam_NSplits) {
        this.cvParam_NSplits = cvParam_NSplits;
    }

    public Boolean getCvParam_Shuffle() {
        return cvParam_Shuffle;
    }

    public void setCvParam_Shuffle(Boolean cvParam_Shuffle) {
        this.cvParam_Shuffle = cvParam_Shuffle;
    }

    public Boolean getCvParam_NeedCv() {
        return cvParam_NeedCv;
    }

    public void setCvParam_NeedCv(Boolean cvParam_NeedCv) {
        this.cvParam_NeedCv = cvParam_NeedCv;
    }

    public Integer getValidationFreqs() {
        return validationFreqs;
    }

    public void setValidationFreqs(Integer validationFreqs) {
        this.validationFreqs = validationFreqs;
    }

    public Integer getEarlyStoppingRounds() {
        return earlyStoppingRounds;
    }

    public void setEarlyStoppingRounds(Integer earlyStoppingRounds) {
        this.earlyStoppingRounds = earlyStoppingRounds;
    }
//endregion
}
