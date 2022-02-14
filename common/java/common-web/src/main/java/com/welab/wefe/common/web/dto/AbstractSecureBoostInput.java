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

package com.welab.wefe.common.web.dto;

import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.util.List;

/**
 * @author lonnie
 */
public class AbstractSecureBoostInput extends AbstractCheckModel {

    @Check(require = true)
    private TreeParam treeParam;

    public TreeParam getTreeParam() {
        return treeParam;
    }

    public void setTreeParam(TreeParam treeParam) {
        this.treeParam = treeParam;
    }

    public static class TreeParam extends AbstractCheckModel {
        @Check(name = "xgboost标准函数正则项系数", require = true)
        private String criterionMethod;

        @Check(name = "xgboost标准函数正则项系数", require = true)
        private List<Double> criterionParams;

        @Check(name = "单棵树的最大深度", require = true)
        private int maxDepth;

        @Check(name = "分裂一个内部节点(非叶子节点)需要的最小样本数", require = true)
        private int minSampleSplit;

        @Check(name = "每个叶子节点包含的最小样本数", require = true)
        private int minLeafNode;

        @Check(name = "单次拆分的要达到的最小增益", require = true)
        private float minImpuritySplit;

        @Check(name = "可分叉的最大节点数", require = true)
        private int maxSplitNodes;

        public String getCriterionMethod() {
            return criterionMethod;
        }

        public void setCriterionMethod(String criterionMethod) {
            this.criterionMethod = criterionMethod;
        }

        public List<Double> getCriterionParams() {
            return criterionParams;
        }

        public void setCriterionParams(List<Double> criterionParams) {
            this.criterionParams = criterionParams;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public void setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
        }

        public int getMinSampleSplit() {
            return minSampleSplit;
        }

        public void setMinSampleSplit(int minSampleSplit) {
            this.minSampleSplit = minSampleSplit;
        }

        public int getMinLeafNode() {
            return minLeafNode;
        }

        public void setMinLeafNode(int minLeafNode) {
            this.minLeafNode = minLeafNode;
        }

        public float getMinImpuritySplit() {
            return minImpuritySplit;
        }

        public void setMinImpuritySplit(float minImpuritySplit) {
            this.minImpuritySplit = minImpuritySplit;
        }

        public int getMaxSplitNodes() {
            return maxSplitNodes;
        }

        public void setMaxSplitNodes(int maxSplitNodes) {
            this.maxSplitNodes = maxSplitNodes;
        }
    }

    @Check(require = true)
    private ObjectiveParam objectiveParam;

    public ObjectiveParam getObjectiveParam() {
        return objectiveParam;
    }

    public void setObjectiveParam(ObjectiveParam objectiveParam) {
        this.objectiveParam = objectiveParam;
    }

    public static class ObjectiveParam extends AbstractCheckModel {
        @Check(name = "损失函数", require = true)
        private String objective;

        @Check(name = "损失函数正则项系数", require = true)
        private List<Double> params;

        public String getObjective() {
            return objective;
        }

        public void setObjective(String objective) {
            this.objective = objective;
        }

        public List<Double> getParams() {
            return params;
        }

        public void setParams(List<Double> params) {
            this.params = params;
        }
    }

    @Check(require = true)
    private CvParam cvParam;

    public CvParam getCvParam() {
        return cvParam;
    }

    public void setCvParam(CvParam cvParam) {
        this.cvParam = cvParam;
    }

    public static class CvParam extends AbstractCheckModel {
        @Check(name = "在KFold中使用分割符大小", require = true)
        private int nSplits;

        @Check(name = "在KFold之前是否进行洗牌", require = true)
        private boolean shuffle;

        @Check(name = "是否需要进行此模块", require = true)
        private boolean needCv;

        public int getnSplits() {
            return nSplits;
        }

        public void setnSplits(int nSplits) {
            this.nSplits = nSplits;
        }

        public boolean isShuffle() {
            return shuffle;
        }

        public void setShuffle(boolean shuffle) {
            this.shuffle = shuffle;
        }

        public boolean isNeedCv() {
            return needCv;
        }

        public void setNeedCv(boolean needCv) {
            this.needCv = needCv;
        }
    }

}
