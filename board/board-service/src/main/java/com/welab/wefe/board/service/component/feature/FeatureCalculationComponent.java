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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.filter.IntersectedOutputFilter;
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
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.TaskResultType;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.JObject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lonnie
 */
@Service
public class FeatureCalculationComponent extends AbstractComponent<FeatureCalculationComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        FlowGraphNode intersectionNode = graph.findOneNodeFromParent(node, ComponentType.Intersection);
        if (intersectionNode == null) {
            throw new FlowNodeException(node, "请在前面添加样本对齐组件。");
        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.FeatureCalculation;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        JSONObject taskParam = new JSONObject();

        return taskParam;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        List<TaskResultMySqlModel> list = taskResultService.listAllResult(taskId)
                .stream()
                .filter(x -> x.getType().equals(TaskResultType.model_result))
                .collect(Collectors.toList());

        // Put the reassembled data in
        list.add(getResult(taskId, TaskResultType.model_result.name()));

        return list;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.model_result.name());

        if (taskResult == null) {
            return null;
        }
        TaskResultMySqlModel taskResultMySqlModel = new TaskResultMySqlModel();
        BeanUtils.copyProperties(taskResult, taskResultMySqlModel);

        List<MemberFeatureInfoModel> members = new ArrayList<>();
        JObject result = JObject.create(taskResult.getResult());

        JObject modelParam = result.getJObject("model_param");

        List<JObject> calculateResults = modelParam.getJSONList("calculateResults");

        for (JObject obj : calculateResults) {
            String memberRole = obj.getString("role");
            String memberId = obj.getString("memberId");
            MemberFeatureInfoModel member = new MemberFeatureInfoModel();

            List<JObject> results = obj.getJSONList("results");

            JSONArray ivValue = new JSONArray();
            JSONArray ivCols = new JSONArray();
            JSONArray cvValue = new JSONArray();
            JSONArray cvCols = new JSONArray();
            List<MemberFeatureInfoModel.Feature> features = new ArrayList<>();

            Map<String, Double> cvMap = new HashMap<>();
            Map<String, Double> ivMap = new HashMap<>();

            // Convert the cv/iv value to map form and store
            for (JObject resultObj : results) {

                if ("iv_value_thres".equals(resultObj.getString("filterName"))) {
                    ivValue = resultObj.getJSONArray("values");
                    ivCols = resultObj.getJSONArray("cols");
                }
                if ("coefficient_of_variation_value_thres".equals(resultObj.getString("filterName"))) {
                    cvValue = resultObj.getJSONArray("values");
                    cvCols = resultObj.getJSONArray("cols");
                }


                for (int i = 0; i < ivCols.size(); i++) {
                    ivMap.put(ivCols.getString(i), ivValue.getDoubleValue(i));
                }

                for (int i = 0; i < cvCols.size(); i++) {
                    cvMap.put(cvCols.getString(i), cvValue.getDoubleValue(i));
                }
            }

            // Traverse the iv value map, add the features with the iv value to the feature column,
            // and find the corresponding cv value from the cvMap
            for (Map.Entry<String, Double> entry : ivMap.entrySet()) {
                MemberFeatureInfoModel.Feature feature = new MemberFeatureInfoModel.Feature();
                feature.setName(entry.getKey());
                feature.setIv(entry.getValue());
                if (cvMap.containsKey(entry.getKey())) {
                    feature.setCv(cvMap.get(entry.getKey()));
                }
                features.add(feature);
            }

            // Traverse the map of cv values and add features with cv values but no iv values to the feature column
            for (Map.Entry<String, Double> entry : cvMap.entrySet()) {
                if (ivMap.containsKey(entry.getKey())) {
                    continue;
                }
                MemberFeatureInfoModel.Feature feature = new MemberFeatureInfoModel.Feature();
                feature.setName(entry.getKey());
                feature.setCv(entry.getValue());
                features.add(feature);
            }

            member.setMemberId(memberId);
            member.setMemberName(CacheObjects.getMemberName(member.getMemberId()));
            member.setMemberRole(JobMemberRole.valueOf(memberRole));
            member.setFeatures(features);

            members.add(member);
        }

        taskResultMySqlModel.setResult(JObject.create().append("result", members).toJSONString());
        return taskResultMySqlModel;

    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                InputMatcher.of(Names.Data.NORMAL_DATA_SET, new IntersectedOutputFilter(graph)),
                InputMatcher.of(Names.Model.BINNING_MODEL, IODataType.ModelFromBinning)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.JSON_RESULT, IODataType.Json)
        );
    }

    @Override
    protected boolean needIntersectedDataSetBeforeMe() {
        return true;
    }

    @Override
    public boolean hasParams() {
        return false;
    }

    public static class Params extends AbstractCheckModel {

    }
}
