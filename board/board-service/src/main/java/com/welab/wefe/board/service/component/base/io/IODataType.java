/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.component.base.io;

/**
 * The input and output type of the component
 *
 * @author zane.luo
 */
public enum IODataType {
    /**
     *
     */
    BoardDataSet("原始数据集", DataTypeGroup.Data),
    DataSetInstance("加载后的数据集", DataTypeGroup.Data),

    ModelFromLr("逻辑回归模型", DataTypeGroup.Model),
    ModelFromXGBoost("XGBoost模型", DataTypeGroup.Model),
    ModelFromBinning("分箱后的模型", DataTypeGroup.Model),

    Json("Json Result", DataTypeGroup.Other);

    private final String label;
    private final DataTypeGroup group;

    IODataType(String label, DataTypeGroup group) {
        this.label = label;
        this.group = group;
    }

    public String getLabel() {
        return label;
    }

    public DataTypeGroup getGroup() {
        return group;
    }
}
