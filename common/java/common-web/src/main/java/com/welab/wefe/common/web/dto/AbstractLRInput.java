/*
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

package com.welab.wefe.common.web.dto;

import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

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

}
