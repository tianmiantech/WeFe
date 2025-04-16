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

import java.util.Set;

/**
 * @author lonnie
 */
public class AbstractLRInput extends AbstractCheckModel {

    @Check(require = true)
    private InitParam initParam;

    public InitParam getInitParam() {
        return initParam;
    }

    public void setInitParam(InitParam initParam) {
        this.initParam = initParam;
    }

    public static class InitParam extends AbstractCheckModel {
        @Check(name = "模型初始化方式", require = true)
        private String initMethod;

        @Check(name = "是否需要偏置系数", require = true)
        private String fitIntercept;

        public String getInitMethod() {
            return initMethod;
        }

        public void setInitMethod(String initMethod) {
            this.initMethod = initMethod;
        }

        public String getFitIntercept() {
            return fitIntercept;
        }

        public void setFitIntercept(String fitIntercept) {
            this.fitIntercept = fitIntercept;
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

    @Check(require = true)
    private GridSearchParam gridSearchParam;

    public GridSearchParam getGridSearchParam() {
        return gridSearchParam;
    }

    public void setGridSearchParam(GridSearchParam gridSearchParam) {
        this.gridSearchParam = gridSearchParam;
    }

    public static class GridSearchParam extends AbstractGridSearchParam {
        @Check(name = "批量大小")
        private int[] batchSize;

        @Check(name = "最大迭代次数")
        private int[] maxIter;

        @Check(name = "学习率")
        private float[] learningRate;

        @Check(name = "惩罚项系数")
        private float[] alpha;

        @Check(name = "优化算法")
        private Set<String> optimizer;

        // region getter/setter

        public int[] getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int[] batchSize) {
            this.batchSize = batchSize;
        }

        public int[] getMaxIter() {
            return maxIter;
        }

        public void setMaxIter(int[] maxIter) {
            this.maxIter = maxIter;
        }

        public float[] getLearningRate() {
            return learningRate;
        }

        public void setLearningRate(float[] learningRate) {
            this.learningRate = learningRate;
        }

        public float[] getAlpha() {
            return alpha;
        }

        public void setAlpha(float[] alpha) {
            this.alpha = alpha;
        }

        public Set<String> getOptimizer() {
            return optimizer;
        }

        public void setOptimizer(Set<String> optimizer) {
            this.optimizer = optimizer;
        }


        // endregion
    }
}
