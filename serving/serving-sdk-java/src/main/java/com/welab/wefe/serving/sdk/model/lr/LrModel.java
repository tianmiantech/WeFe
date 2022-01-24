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

package com.welab.wefe.serving.sdk.model.lr;

import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class LrModel {
    private Map<String, Double> weight;
    private Double intercept = 0.0;
    private Integer iters;
    private List<Double> lossHistory;
    private List<String> header;

    public Map<String, Double> getWeight() {
        return weight;
    }

    public void setWeight(Map<String, Double> weight) {
        this.weight = weight;
    }

    public Double getIntercept() {
        return intercept;
    }

    public void setIntercept(Double intercept) {
        this.intercept = intercept;
    }

    public Integer getIters() {
        return iters;
    }

    public void setIters(Integer iters) {
        this.iters = iters;
    }

    public List<Double> getLossHistory() {
        return lossHistory;
    }

    public void setLossHistory(List<Double> lossHistory) {
        this.lossHistory = lossHistory;
    }

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }
}
