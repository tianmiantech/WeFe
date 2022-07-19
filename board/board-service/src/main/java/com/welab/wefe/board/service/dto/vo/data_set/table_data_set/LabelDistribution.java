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
package com.welab.wefe.board.service.dto.vo.data_set.table_data_set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * lable 分布情况
 *
 * @author zane
 * @date 2022/5/12
 */
public class LabelDistribution {
    @Check(name = "标签类别数量")
    public int labelSpeciesCount;
    @Check(name = "标签列表")
    public List<LabelDistributionItem> list = new ArrayList<>();

    public LabelDistribution() {
    }

    public LabelDistribution(int labelSpeciesCount, Map<String, Integer> labelDistribution) {
        this.labelSpeciesCount = labelSpeciesCount;
        labelDistribution.forEach((k, v) -> {
            list.add(new LabelDistributionItem(k, v));
        });

        list.sort((o1, o2) -> o2.count - o1.count);
    }


    public JSONObject toJson() {
        return JSON.parseObject(JSON.toJSONString(this));
    }
}
