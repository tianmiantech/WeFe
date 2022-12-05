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

package com.welab.wefe.board.service.dto.entity;

import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.util.ArrayList;
import java.util.List;

/**
 * 成员信息参数接收实体
 *
 * @author lonnie
 */
public class MemberFeatureInfoModel extends MemberModel {

    @Check(name = "特征列", require = true)
    private List<Feature> features = new ArrayList<>();

    private String dataSetId;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public static class Feature extends AbstractCheckModel {
        @Check(name = "特征名")
        private String name;
        public Double cv;
        public Double iv;
        public Double missingRate;
        private String method;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Double getCv() {
            return cv;
        }

        public void setCv(Double cv) {
            this.cv = cv;
        }

        public Double getIv() {
            return iv;
        }

        public void setIv(Double iv) {
            this.iv = iv;
        }

        public Double getMissingRate() {
            return missingRate;
        }

        public void setMissingRate(Double missingRate) {
            this.missingRate = missingRate;
        }
    }

}
