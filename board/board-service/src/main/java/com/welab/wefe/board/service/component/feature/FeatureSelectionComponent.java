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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.DataIOComponent;
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
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.JObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author lonnie
 */
@Service
public class FeatureSelectionComponent extends AbstractComponent<FeatureSelectionComponent.Params> {

    @Override
    public boolean stopCreateTask(List<FlowGraphNode> preNodes, FlowGraphNode node) throws StatusCodeWithException {
        Params params = (Params) node.getParamsModel();

        // When no feature is selected, stop creating the task.
        if (CollectionUtils.isNotEmpty(params.members) && params.members.size() > 0) {
            boolean selectFeature = false;
            for (MemberFeatureInfoModel member : params.members) {
                if (CollectionUtils.isNotEmpty(member.getFeatures()) && member.getFeatures().size() > 0) {
                    selectFeature = true;
                }
            }
            // No feature selected, stop creating task.
            return !selectFeature;
        } else {
            // If there is no member node, then there is no feature, stop creating tasks.
            return true;
        }
    }

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

        if (count.get() != dataSetItems.size()) {
            throw new FlowNodeException(node, "请保证当前节点所有成员都参与。");
        }

        if (graph.getJob().getFederatedLearningType() == FederatedLearningType.horizontal) {
            List<MemberFeatureInfoModel> members = params.getMembers();
            List<List<String>> featuresList = new ArrayList<>();
            members.forEach(member -> {
                List<MemberFeatureInfoModel.Feature> features = member.getFeatures();
                List<String> featureNames = new ArrayList<>();
                features.forEach(feature -> featureNames.add(feature.getName()));
                featuresList.add(featureNames);
            });

            for (int i = 0; i < featuresList.size() - 1; i++) {
                if (!CollectionUtils.isEqualCollection(featuresList.get(i), featuresList.get(i + 1))) {
                    throw new FlowNodeException(node, "横向建模需要保证所有样本所选特征列表一致。");
                }
            }

        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.FeatureSelection;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        JSONObject taskParam = new JSONObject();

        // Reassemble front-end parameters
        List<MemberFeatureInfoModel> members = params.members;
        List<JObject> kernelParam = new ArrayList<>();
        for (MemberFeatureInfoModel member : members) {

            JObject obj = JObject.create().append("member_id", member.getMemberId())
                    .append("role", member.getMemberRole())
                    .append("features", member.getFeatures().stream().map(x -> x.getName()).collect(Collectors.toList()));

            kernelParam.add(obj);
        }
        taskParam.put("params", JObject.create().append("members", kernelParam));

        taskParam.put("env", "test");

        return taskParam;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        return null;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
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

    @Override
    public boolean canSelectFeatures() {
        return true;
    }

}
