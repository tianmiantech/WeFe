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
package com.welab.wefe.common.wefe;

import com.welab.wefe.common.Validator;
import com.welab.wefe.common.wefe.enums.ColumnDataType;

import java.util.*;
import java.util.function.Consumer;

/**
 * 字段类型推理
 *
 * @author zane
 * @date 2022/4/8
 */
public class ColumnDataTypeInferrer implements Consumer<LinkedHashMap<String, Object>> {
    private final static List<String> NULL_VALUE_LIST = Arrays.asList("", "null", "NA", "nan", "None");
    private List<String> columns;
    /**
     * 数据样本
     */
    private List<Map<String, Object>> samples = new ArrayList<>();
    /**
     * 最终的推理结果
     */
    private final LinkedHashMap<String, ColumnDataType> result = new LinkedHashMap<>();
    /**
     * 推理过程中的中间信息
     */
    private final LinkedHashMap<String, Set<ColumnDataType>> columnDataTypes = new LinkedHashMap<>();

    /**
     * @param columns 需要推理的字段列表
     */
    public ColumnDataTypeInferrer(List<String> columns) {
        this.columns = columns;
        for (String name : columns) {
            result.put(name, null);
            columnDataTypes.put(name, new HashSet<>());
        }
    }

    public List<String> getColumnNames() {
        return columns;
    }

    @Override
    public void accept(LinkedHashMap<String, Object> row) {
        if (samples.size() < 10) {
            samples.add(row);
        }

        // 如果所有字段已推理出结果，则不再接收数据进行推理。
        if (columnDataTypes.isEmpty()) {
            return;
        }

        Iterator<String> iterator = columnDataTypes.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();

            Object value = row.get(name);
            // 根据字段的值，推理出字段的类型
            ColumnDataType dataType = inferDataType(String.valueOf(value));
            if (dataType == null) {
                continue;
            }

            // 记录该字段发现的所有数据类型
            this.columnDataTypes.get(name).add(dataType);

            // 如果当前值是 String，则无需继续推理。
            if (dataType == ColumnDataType.String) {
                result.put(name, dataType);
            }

            // 将已推理出结论的字段移除
            if (result.get(name) != null) {
                iterator.remove();
            }
        }

    }

    /**
     * 结束推理，根据当前线索推理出所有字段的数据类型。
     */
    public Map<String, ColumnDataType> getResult() {
        Iterator<String> iterator = columnDataTypes.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            Set<ColumnDataType> types = this.columnDataTypes.get(name);

            if (types.isEmpty()) {
                continue;
            } else if (types.contains(ColumnDataType.String)) {
                result.put(name, ColumnDataType.String);
            } else if (types.contains(ColumnDataType.Enum)) {
                result.put(name, ColumnDataType.Enum);
            } else if (types.contains(ColumnDataType.Double)) {
                result.put(name, ColumnDataType.Double);
            } else if (types.contains(ColumnDataType.Long)) {
                result.put(name, ColumnDataType.Long);
            } else if (types.contains(ColumnDataType.Integer)) {
                result.put(name, ColumnDataType.Integer);
            } else {
                throw new RuntimeException("执行到这里说明在未来的有一天，新增了数据类型，这里的代码要做相应的修改。");
            }

            iterator.remove();
        }
        return result;
    }

    /**
     * Infer data type
     */
    private ColumnDataType inferDataType(String value) {
        if (isEmptyValue(value)) {
            return null;
        }

        if (Validator.isInteger(value)) {
            return ColumnDataType.Integer;
        }

        if (Validator.isLong(value)) {
            return ColumnDataType.Long;
        }

        if (Validator.isDouble(value)) {
            return ColumnDataType.Double;
        }

        return ColumnDataType.String;
    }

    public List<Map<String, Object>> getSamples() {
        return samples;
    }

    public static boolean isEmptyValue(String value) {
        return NULL_VALUE_LIST.stream().anyMatch(x -> x.equalsIgnoreCase(value));
    }

}
