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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.component.feature.VertPCAComponent.Params.MemberInfoModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.MemberModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.TaskResultType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.dto.AbstractApiInput;

@Service
public class VertPCAComponent extends AbstractComponent<VertPCAComponent.Params> {
    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                        Params params) throws FlowNodeException {
        FlowGraphNode intersectionNode = graph.findOneNodeFromParent(node, ComponentType.Intersection);
        if (intersectionNode == null) {
            throw new FlowNodeException(node, "请在前面添加样本对齐组件。");
        }
        
        List<MemberInfoModel> members = params.getMembers();
        if (members.size() > 2) {
            throw new FlowNodeException(node, "两方纵向PCA参与成员不能超过两方。");
        }
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                          Params params) throws FlowNodeException {

        JSONObject taskParam = new JSONObject();

        JObject resultObj = JObject.create();
        List<String> featureList = new ArrayList<>();
        params.getMembers().forEach(member -> {
            if (CacheObjects.getMemberId().equals(member.getMemberId())
                    && graph.getJob().getMyRole() == member.getMemberRole()) {
                List<String> features = member.getFeatures();
                features.forEach(x -> featureList.add(x));
            }
        });
        resultObj.append("column_names", featureList);

        taskParam.put("params", resultObj);

        return taskParam;
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.VertPCA;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return null;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        return taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_statistics.name());
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(OutputItem.of(Names.JSON_RESULT, IODataType.Json));
    }

    public static class Params extends AbstractApiInput {

        @Check(name = "成员信息", require = true)
        private List<MemberInfoModel> members;

        private boolean crossParties;

        public List<MemberInfoModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberInfoModel> members) {
            this.members = members;
        }

        public boolean getCrossParties() {
            return crossParties;
        }

        public static class MemberInfoModel extends MemberModel {
            @Check(name = "特征列", require = true)
            private List<String> features = new ArrayList<>();

            public List<String> getFeatures() {
                return features;
            }

            public void setFeatures(List<String> features) {
                this.features = features;
            }

        }
    }
}
