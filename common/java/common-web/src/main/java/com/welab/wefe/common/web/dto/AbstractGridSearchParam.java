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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.JObject;

/**
 * @author zane.luo
 * @date 2022/9/27
 */
public abstract class AbstractGridSearchParam extends AbstractCheckModel {
    private Boolean needGridSearch = false;

    /**
     * 转换为 kernel 需要的结构
     * <p>
     * e.g：
     * <p>
     * "grid_search_param": {
     * "params_list": {
     * "optimizer": ["sgd","adam"],
     * "max_iter": [30,50,100],
     * "batch_size": [320,500,1000],
     * "learning_rate": [0.001,0.01,0.015],
     * "alpha": [0.01,0.1]
     * },
     * "need_grid_search": false
     * }
     */
    @JSONField(serialize = false)
    public JSONObject toKernelParam() {

        JObject json = JObject.create(this);
        json.remove("need_grid_search");
        json.entrySet().removeIf(item -> {
            Object value = item.getValue();
            if (value == null) {
                return true;
            }

            // 空数组也删除
            if (value instanceof JSONArray) {
                if (((JSONArray) value).isEmpty()) {
                    return true;
                }
            }
            return false;
        });

        return JObject
                .create()
                .append("need_grid_search", needGridSearch)
                .append("params_list", json);
    }

    // region getter/setter

    public Boolean getNeedGridSearch() {
        return needGridSearch;
    }

    public void setNeedGridSearch(Boolean needGridSearch) {
        this.needGridSearch = needGridSearch;
    }


    // endregion
}
