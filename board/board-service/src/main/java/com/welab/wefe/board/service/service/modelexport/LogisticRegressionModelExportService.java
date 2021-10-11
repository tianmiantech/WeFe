/**
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

package com.welab.wefe.board.service.service.modelexport;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.util.JObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logistic regression model export service
 *
 * @author aaron.li
 **/
@Service
public class LogisticRegressionModelExportService {

    /**
     * export model
     *
     * @param modelParam model param
     * @param language   language
     */
    public String export(JObject modelParam, String language) {
        // Get the corresponding language interpreter
        BaseLogisticRegressionLanguage baseLogisticRegressionLanguage = getXgboostLanguage(language);
        JSONArray headerArray = modelParam.getJSONArray("header");
        String intercept = modelParam.getString("intercept");
        JObject weight = modelParam.getJObject("weight");

        List<String> headers = new ArrayList<>();
        for (Object header : headerArray) {
            headers.add(String.valueOf(header));
        }

        Map<String, String> weightMap = new HashMap<>(16);
        for (Map.Entry<String, Object> entry : weight.getInnerMap().entrySet()) {
            weightMap.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        return baseLogisticRegressionLanguage.generateMethodCode(headers, weightMap, intercept);
    }


    /**
     * language interpreter
     */
    private BaseLogisticRegressionLanguage getXgboostLanguage(String language) {
        return new LogisticRegressionLanguageSelector(language).getSelector();
    }
}
