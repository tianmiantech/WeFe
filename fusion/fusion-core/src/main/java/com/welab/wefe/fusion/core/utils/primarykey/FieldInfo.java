/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.fusion.core.utils.primarykey;

import com.welab.wefe.fusion.core.enums.Options;

import java.util.Arrays;
import java.util.List;

/**
 * @author hunter.zhao
 */
public class FieldInfo {
    private String columns;

    private Options options;

    private Integer fristIndex;

    private Integer endIndex;

    private Integer position;

    public String getColumns() {
        return columns;
    }

    public List<String> getColumnList() {
        return Arrays.asList(columns.split(","));
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public Integer getFristIndex() {
        return fristIndex;
    }

    public void setFristIndex(Integer fristIndex) {
        this.fristIndex = fristIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
