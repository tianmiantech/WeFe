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

package com.welab.wefe.board.service.component.feature;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.MemberFeatureInfoModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Longitudinal Pearson correlation coefficient between the two parties based on the spdz agreement
 *
 * @author Winter
 */
@Service
public class VertPearsonComponent extends AbstractComponent<VertPearsonComponent.Params> {
    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        List<MemberFeatureInfoModel> members = params.getMembers();
        if (members.size() > 2) {
            throw new FlowNodeException(node, "皮尔逊相关性参与成员不能超过两方。");
        }

        FlowGraphNode intersectionNode = graph.findOneNodeFromParent(node, ComponentType.Intersection);
        if (intersectionNode == null) {
            throw new FlowNodeException(node, "请在前面添加样本对齐组件。");
        }
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        // Need to find DataIO data set
        FlowGraphNode dataIONode = graph.findOneNodeFromParent(node, ComponentType.DataIO);
        TaskMySqlModel dataIOTask = findTaskFromPretasks(preTasks, dataIONode);
        if (dataIONode == null || dataIOTask == null) {
            throw new FlowNodeException(node, "请添加DataIO组件!");
        }

        JObject output = JObject.create();

        params.getMembers().forEach(x -> {
            if (x.getMemberId().equals(CacheObjects.getMemberId()) && graph.getJob().getMyRole() == x.getMemberRole()) {
                List<String> features = new ArrayList<>();
                x.getFeatures().forEach(feature -> {
                    if (StringUtil.isNotEmpty(feature.getMethod())) {
                        features.add(feature.getName());
                    }
                });
                output.append("column_names", features);
            }

        });

        output.append("cross_parties", params.isCrossParties());

        return output;
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.VertPearson;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        return taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_statistics.name());
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
                OutputItem.of(Names.JSON_RESULT, IODataType.Json)
        );
    }

    public static class Params extends AbstractCheckModel {

        @Check(name = "需要计算的特征列名", require = true)
        private List<MemberFeatureInfoModel> members;

        @Check(name = "特征列索引")
        private List<String> columnIndexes;

        @Check(name = "是否要与另一方特征计算相关性系数", require = true, desc = "若为True，need_run必须为True")
        private boolean crossParties;

        public List<MemberFeatureInfoModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberFeatureInfoModel> members) {
            this.members = members;
        }

        public List<String> getColumnIndexes() {
            return columnIndexes;
        }

        public void setColumnIndexes(List<String> columnIndexes) {
            this.columnIndexes = columnIndexes;
        }

        public boolean isCrossParties() {
            return crossParties;
        }

        public void setCrossParties(boolean crossParties) {
            this.crossParties = crossParties;
        }

    }

    @Override
    public boolean canSelectFeatures() {
        return true;
    }

}
