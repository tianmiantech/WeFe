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

package com.welab.wefe.board.service.dto.vo;

import com.welab.wefe.board.service.dto.entity.MemberModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.util.List;

/**
 * @author zane
 * @date 2022/11/9
 */
public class FlowDataSetOutputModel extends MemberModel {
    @Check(name = "数据集 Id")
    private String dataSetId;
    private List<FeatureOutput> features;

    // region getter/setter


    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public List<FeatureOutput> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeatureOutput> features) {
        this.features = features;
    }


    // endregion

}
