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

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.Stopwatch;
import com.welab.wefe.common.Validator;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.wefe.enums.ColumnDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * 字段类型推理
 *
 * @author zane
 * @date 2022/4/8
 */
public class ColumnDataTypeInferrer implements Consumer<LinkedHashMap<String, Object>> {
    private final static List<String> NULL_VALUE_LIST = Arrays.asList("", "null", "NA", "nan", "None");
    /**
     * 字段列表
     */
    private List<String> columns;
    /**
     * 电子围栏，用于检查多线程推理时所有线程运行完毕。
     */
    LongAdder counter = new LongAdder();
    /**
     * 数据样本
     */
    private List<Map<String, Object>> samples = new ArrayList<>();
    /**
     * 最终的推理结果
     */
    private final LinkedHashMap<String, ColumnDataType> result = new LinkedHashMap<>();
    /**
     * 推理过程中的中间信息：各字段的类型列表
     */
    private final Map<String, Set<ColumnDataType>> columnDataTypes = new ConcurrentHashMap<>();
    /**
     * 推理过程中的中间信息：各字段有推出结果的次数
     */
    private final Map<String, LongAdder> columnDataTypeInferCountMap = new ConcurrentHashMap<>();
    /**
     * 计时器，用于开发过程中调测性能。
     */
    private Stopwatch stopwatch = Stopwatch.startNew();

    /**
     * @param columns 需要推理的字段列表
     */
    public ColumnDataTypeInferrer(List<String> columns) {
        stopwatch.tapAndPrint("初始化");
        this.columns = columns;
        for (String name : columns) {
            result.put(name, null);
            columnDataTypes.put(name, new HashSet<>());
            columnDataTypeInferCountMap.put(name, new LongAdder());
        }
        stopwatch.tapAndPrint("初始化完成");
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
        if (columnDataTypeInferCountMap.isEmpty()) {
            return;
        }


        counter.increment();
        CommonThreadPool.run(() -> inferRow(row));

    }

    private void inferRow(LinkedHashMap<String, Object> row) {
        Iterator<String> iterator = columnDataTypeInferCountMap.keySet().iterator();
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

            // 当前字段已成功推理的次数
            LongAdder columnInferredCount = columnDataTypeInferCountMap.get(name);
            // 由于这里是多线程，所以可能其它线程已经删除了 MAP 中当前 name 对应的记录。
            if (columnInferredCount == null) {
                continue;
            }

            columnInferredCount.increment();
            // 已推理过10行，则认为该字段的类型已经确定，不再继续推理。
            if (columnInferredCount.sum() >= 10) {
                iterator.remove();
            }
        }
        counter.decrement();
    }

    /**
     * 结束推理，根据当前线索敲定出所有字段的数据类型。
     */
    public Map<String, ColumnDataType> getResult() {
        stopwatch.tapAndPrint("start getResult");
        // 等待所有线程运行完毕
        while (counter.sum() > 0) {
            ThreadUtil.sleep(10);
        }

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
            } else if (types.contains(ColumnDataType.Boolean)) {
                result.put(name, ColumnDataType.Boolean);
            } else {
                throw new RuntimeException("执行到这里说明在未来的有一天，新增了数据类型，这里的代码要做相应的修改。");
            }

            iterator.remove();
        }
        stopwatch.tapAndPrint("end getResult");
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

        if (Validator.isBoolean(value)) {
            return ColumnDataType.Boolean;
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
