
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

package com.welab.wefe.mpc.sa.request;

import com.welab.wefe.mpc.commom.Operator;

import java.util.List;

/**
 * @Author eval
 * @Date 2021/12/17
 **/
public class QuerySAResultRequest {
    /**
     * 请求标识
     */
    private String uuid;
    /**
     * 所有参与方的DH公钥
     */
    private List<String> diffieHellmanValues;
    /**
     * 操作符，+ or -
     */
    private Operator operator = Operator.ADD;
    /**
     * 权重
     */
    private float weight = 1.0f;
    /**
     * 当前参与方序号
     * [0, diffieHellmanValues.size() - 1]
     */
    private int index;
    /**
     * DH的mode
     */
    private String p;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<String> getDiffieHellmanValues() {
        return diffieHellmanValues;
    }

    public void setDiffieHellmanValues(List<String> diffieHellmanValues) {
        this.diffieHellmanValues = diffieHellmanValues;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
