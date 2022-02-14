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
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.filter.OutputDataTypesOutputFilter;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.MemberModel;
import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.board.service.dto.kernel.machine_learning.KernelTask;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Characteristic statistics component
 *
 * @author lonnie
 */
@Service
public class FeatureStatisticsComponent extends AbstractComponent<FeatureStatisticsComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        // Get the members selected in dataIO
        DataIOComponent.Params dataIOParams = (DataIOComponent.Params) graph.findOneNodeFromParent(node, ComponentType.DataIO).getParamsModel();
        List<DataIOComponent.DataSetItem> dataSetItems = dataIOParams.getDataSetList();

        AtomicInteger count = new AtomicInteger();

        dataSetItems.forEach(x ->
                params.getMembers().forEach(y -> {
                    if (x.getMemberId().equals(y.getMemberId()) && x.getMemberRole() == y.getMemberRole()) {
                        count.addAndGet(1);
                    }
                })
        );

        if (!"local".equals(params.workMode) && count.get() != dataSetItems.size()) {
            throw new FlowNodeException(node, "请保证当前节点所有成员都参与。");
        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.FeatureStatistic;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        // Need to use dataIO data set
        FlowGraphNode dataIONode = graph.findOneNodeFromParent(node, ComponentType.DataIO);

        if (dataIONode == null) {
            throw new FlowNodeException(node, "请添加DataIO策略!");
        }

        List<Method> featureMethods = params.getFeatureMethods();
        List percentileList = new ArrayList();
        for (Method method : featureMethods) {
            if ("percentile".equals(method.getName())) {
                percentileList.add(Integer.parseInt(method.getValue()));
            }
        }

        JObject output = JObject.create()
                .append("percentage_list", percentileList)
                .append("work_mode", params.workMode);
        return output;
    }

    @Override
    public boolean canSelectFeatures() {
        return true;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        List<TaskResultMySqlModel> list = taskResultService.listAllResult(taskId)
                .stream()
                .filter(x -> x.getType().equals(TaskResultType.data_feature_statistic))
                .collect(Collectors.toList());


        // Put the reassembled data in
        list.add(getResult(taskId, TaskResultType.data_feature_statistic.name()));

        return list;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.data_feature_statistic.name());

        if (taskResult == null) {
            return null;
        }
        TaskResultMySqlModel taskResultMySqlModel = new TaskResultMySqlModel();
        BeanUtils.copyProperties(taskResult, taskResultMySqlModel);

        JObject resultObj = JObject.create(taskResultMySqlModel.getResult().replace("-Infinity", "-99999").replace("Infinity", "99999"));
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
        return Arrays.asList(
                InputMatcher.of(
                        Names.Data.NORMAL_DATA_SET,
                        new OutputDataTypesOutputFilter(
                                graph,
                                IODataType.DataSetInstance
                        ))
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.JSON_RESULT, IODataType.Json)
        );
    }

    @Override
    public KernelTask getTaskMembers(FlowGraph graph, FlowGraphNode node) {
        Params params = ((Params) node.getParamsModel());
        List<Member> members = new ArrayList<>();
        if ("local".equals(params.getWorkMode())) {
            params.getMembers().forEach(x -> {
                if (x.getMemberId().equals(CacheObjects.getMemberId())) {
                    Member member = new Member();
                    member.setMemberId(x.getMemberId());
                    member.setMemberName(x.getMemberName());
                    member.setMemberRole(x.getMemberRole());
                    members.add(member);
                }
            });
            return new KernelTask(members);
        } else {
            return super.getTaskMembers(graph, node);
        }
    }

    public static class Params extends AbstractCheckModel {
        @Check(require = true)
        private List<Method> featureMethods;

        @Check(name = "模式状态")
        private String workMode;

        private List<MemberModel> members;

        public List<Method> getFeatureMethods() {
            return featureMethods;
        }

        public void setFeatureMethods(List<Method> featureMethods) {
            this.featureMethods = featureMethods;
        }

        public String getWorkMode() {
            return workMode;
        }

        public void setWorkMode(String workMode) {
            this.workMode = workMode;
        }

        public List<MemberModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberModel> members) {
            this.members = members;
        }
    }

    public static class Method extends AbstractCheckModel {
        @Check(require = true)
        private String name;

        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
