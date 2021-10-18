/**
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

package com.welab.wefe.board.service.component.feature;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.filter.OutputDataTypesOutputFilter;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.MemberModel;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.TaskResultType;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.JObject;

/**
 * @author zane.luo
 */
@Service
public class MixStatisticComponent extends AbstractComponent<MixStatisticComponent.Params> {
    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
            Params params) {
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.MixStatistic;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
            Params params) {
        JSONObject taskParam = new JSONObject();
        List<MemberFeatureInfoModel> members = params.members;
        for (MemberFeatureInfoModel member : members) {
            if (CacheObjects.getMemberId().equals(member.getMemberId())) {
                List<String> features = member.features;
                taskParam.put("params", JObject.create("col_names", features));
            }
        }
        return taskParam;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        List<TaskResultMySqlModel> list = taskResultService.listAllResult(taskId).stream()
                .filter(x -> x.getType().equals(TaskResultType.data_feature_statistic.name()))
                .collect(Collectors.toList());

        // Put the reassembled data in
        list.add(getResult(taskId, TaskResultType.data_feature_statistic.name()));

        return list;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndType(taskId,
                TaskResultType.data_feature_statistic.name());

        if (taskResult == null) {
            return null;
        }
        TaskResultMySqlModel taskResultMySqlModel = new TaskResultMySqlModel();
        BeanUtils.copyProperties(taskResult, taskResultMySqlModel);

        JObject resultObj = JObject
                .create(taskResultMySqlModel.getResult().replace("-Infinity", "-99999").replace("Infinity", "99999"));
        List<JObject> members = resultObj.getJSONList("members");
        if (CollectionUtils.isNotEmpty(members)) {
            members.forEach(x -> {
                String memberId = x.getString("member_id");
                String memberName = CacheObjects.getMemberName(memberId);
                x.put("member_name", memberName);
            });
        }
        resultObj.put("members", members);
        taskResultMySqlModel.setResult(resultObj.toJSONString());

        return taskResultMySqlModel;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(InputMatcher.of(Names.Data.NORMAL_DATA_SET,
                new OutputDataTypesOutputFilter(graph, IODataType.DataSetInstance)));
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(OutputItem.of(Names.JSON_RESULT, IODataType.Json));
    }

    public static class Params extends AbstractCheckModel {

        private List<MemberFeatureInfoModel> members;

        public List<MemberFeatureInfoModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberFeatureInfoModel> members) {
            this.members = members;
        }

    }

    public static class MemberFeatureInfoModel extends MemberModel {

        private List<String> features;

        public List<String> getFeatures() {
            return features;
        }

        public void setFeatures(List<String> features) {
            this.features = features;
        }

    }

}
