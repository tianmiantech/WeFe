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

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.MemberModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.dto.AbstractApiInput;

@Service
public class VertFilterComponent extends AbstractComponent<VertFilterComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                        Params params) throws FlowNodeException {
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
                                          Params params) throws FlowNodeException {
        JSONObject taskParam = new JSONObject();
        JObject resultObj = JObject.create();
        params.getMembers().forEach(member -> {
            if (CacheObjects.getMemberId().equals(member.getMemberId())
                    && graph.getJob().getMyRole() == member.getMemberRole()) {
                resultObj.append("filter_rules", member.getFilterRules());
            }
        });

        taskParam.put("params", resultObj);
        return taskParam;
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.VertFilter;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return null;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        return null;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
    }

    public static class Params extends AbstractApiInput {

        @Check(name = "成员信息", require = true)
        private List<MemberInfoModel> members;

        public List<MemberInfoModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberInfoModel> members) {
            this.members = members;
        }

        public static class MemberInfoModel extends MemberModel {
            @Check(name = "过滤规则", desc = "支持 >,<,>=,<=,=,!=")
            private String filterRules;

            public String getFilterRules() {
                return filterRules;
            }

            public void setFilterRules(String filterRules) {
                this.filterRules = filterRules;
            }
        }
    }
}
