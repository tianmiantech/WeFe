/*
 * Copyright 2022 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.mpc.psi.sdk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfuseData {

    private String singleFieldName;
    private List<String> mixFieldNames = new ArrayList<>();;
    private boolean isJson = false;
    private Function<String, List<String>> generateDataFunc;

    public List<String> generateConfuseData(String s) {
        if (generateDataFunc != null) {
            return generateDataFunc.apply(s);
        } else {
            return new ArrayList<>();
        }
    }

    public String getSingleFieldName() {
        return singleFieldName;
    }

    public void setSingleFieldName(String singleFieldName) {
        this.singleFieldName = singleFieldName;
    }

    public List<String> getMixFieldNames() {
        return mixFieldNames;
    }

    public void setMixFieldNames(List<String> mixFieldNames) {
        this.mixFieldNames = mixFieldNames;
    }

    public boolean isJson() {
        return isJson;
    }

    public void setJson(boolean isJson) {
        this.isJson = isJson;
    }

    public Function<String, List<String>> getGenerateDataFunc() {
        return generateDataFunc;
    }

    public void setGenerateDataFunc(Function<String, List<String>> generateDataFunc) {
        this.generateDataFunc = generateDataFunc;
    }

}
