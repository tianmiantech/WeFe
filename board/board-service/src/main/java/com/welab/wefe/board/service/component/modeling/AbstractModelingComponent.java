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

package com.welab.wefe.board.service.component.modeling;

import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.filter.OutputItemFilterFunction;
import com.welab.wefe.board.service.component.base.io.*;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
public abstract class AbstractModelingComponent<T extends AbstractCheckModel> extends AbstractComponent<T> {

    /**
     * Find the delegate of the training data set
     */
    OutputItemFilterFunction TRAIN_DATA_SET_FILTER = (n, item) -> ComponentType.Segment == n.getComponentType();

    /**
     * Find test/validation data set commission
     */
    protected final InputSupplier TEST_DATA_SET_SUPPLIER = (graph, node) -> {

        // Find validation set
        FlowGraphNode validationNode = graph.findValidationDataSetFromParent(node, taskType());
        if (validationNode != null) {
            return new NodeOutputItem(validationNode, OutputItem.of(Names.Data.EVALUATION_DATA_SET, IODataType.DataSetInstance));
        }

        // Find data cutting
        FlowGraphNode segmentNode = graph.findOneNodeFromParent(node, ComponentType.Segment);
        if (segmentNode != null) {
            return new NodeOutputItem(segmentNode, OutputItem.of(Names.Data.EVALUATION_DATA_SET, IODataType.DataSetInstance));
        }

        return null;
    };

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        TaskResultMySqlModel result = new TaskResultMySqlModel();
        JObject resultObj = JObject.create();

        TaskResultMySqlModel metricTrainResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_train.name());
        TaskResultMySqlModel modelTrainResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.model_train.name());

        if (metricTrainResult != null) {
            BeanUtils.copyProperties(metricTrainResult, result);
        } else if (modelTrainResult != null) {
            BeanUtils.copyProperties(modelTrainResult, result);
        } else {
            return null;
        }

        if (metricTrainResult != null) {
            resultObj = JObject.create(metricTrainResult.getResult());
            JObject trainLoss = resultObj.getJObject("train_loss");
            if (trainLoss != null) {

                JObject data = trainLoss.getJObject("data");
                if (data != null) {
                    List<Double> lossArray = new ArrayList<>();
                    List<String> list = data
                            .keySet()
                            .stream()
                            .filter(x -> !"Best".equals(x))
                            .sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList());

                    // The sort tool method does not work, so I wrote the following sort code.
                    for (int i = 0; i < list.size() - 1; i++) {
                        for (int j = i + 1; j < list.size(); j++) {
                            if (Integer.parseInt(list.get(i)) > Integer.parseInt(list.get(j))) {
                                String tep = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tep);
                            }
                        }
                    }

                    for (String str : list) {
                        lossArray.add(data.getJObject(str).getDouble("value"));
                    }
                    trainLoss.put("data", lossArray);
                    resultObj.put("train_loss", trainLoss);
                }
            }
        }

        if (modelTrainResult != null) {
            resultObj.putAll(JObject.create(modelTrainResult.getResult()));
        }

        result.setResult(resultObj.toJSONString());

        return result;
    }

}
