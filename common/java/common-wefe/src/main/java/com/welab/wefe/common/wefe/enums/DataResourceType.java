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
package com.welab.wefe.common.wefe.enums;

/**
 * @author zane
 * @date 2021/11/16
 */
public enum DataResourceType {
    /**
     * 二维表结构的数据集，用于常规机器学习（LR、XGB）
     */
    TableDataSet("结构化数据集"),
    /**
     * 图片形式的数据集，用于深度学习。
     */
    ImageDataSet("图像数据集"),
    /**
     * 布隆过滤器
     */
    BloomFilter("过滤器数据");

    private final String label;

    DataResourceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
