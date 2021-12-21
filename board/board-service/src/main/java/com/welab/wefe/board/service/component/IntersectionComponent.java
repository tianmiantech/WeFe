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

package com.welab.wefe.board.service.component;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lonnie
 */
@Service
class IntersectionComponent extends AbstractComponent<IntersectionComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

    }


    @Override
    public ComponentType taskType() {
        return ComponentType.Intersection;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        // Reassemble front-end parameters
        JObject output = JObject.create();
        output
                .append("intersect_method", params.getIntersectMethod())
                .append("save_dataset", params.isSaveDataSet());

        return output;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        List<TaskResultMySqlModel> list = taskResultService.listAllResult(taskId)
                .stream()
                .filter(x -> x.getType().equals(TaskResultType.metric_train))
                .collect(Collectors.toList());

        // Put the reassembled data in
        list.add(getResult(taskId, TaskResultType.metric_train.name()));

        return list;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel resultModel = taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_train.name());

        if (resultModel == null) {
            return null;
        }
        TaskResultMySqlModel result = new TaskResultMySqlModel();
        BeanUtils.copyProperties(resultModel, result);

        JObject obj = JObject.create(resultModel.getResult());

        int count = obj.getIntegerByPath("train_intersection.data.count.value");
        int intersect_count = obj.getIntegerByPath("train_intersection.data.intersect_count.value");
        double intersect_rate = obj.getDoubleByPath("train_intersection.data.intersect_rate.value");

        JObject resultObj = JObject.create()
                .append("count", count)
                .append("intersect_count", intersect_count)
                .append("intersect_rate", intersect_rate);

        result.setResult(resultObj.toJSONString());

        return result;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance)
        );
    }

    public static class Params extends AbstractCheckModel {
        @Check(name = "对齐方式", require = true)
        private String intersectMethod;

        private boolean saveDataSet;

        public String getIntersectMethod() {
            return intersectMethod;
        }

        public void setIntersectMethod(String intersectMethod) {
            this.intersectMethod = intersectMethod;
        }

        public boolean isSaveDataSet() {
            return saveDataSet;
        }

        public void setSaveDataSet(boolean saveDataSet) {
            this.saveDataSet = saveDataSet;
        }
    }
}
