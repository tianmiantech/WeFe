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
package com.welab.wefe.board.service.component.feature;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.filter.OutputDataTypeAndNameOutputFilter;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.model.JobBuilder;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author zane
 * @date 2022/10/11
 */
@Service
public class FeaturePsiComponent extends AbstractComponent<FeaturePsiComponent.Params> {

    @Override
    public boolean canSelectFeatures() {
        return true;
    }

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        if (graph.getJob().getFederatedLearningType() != FederatedLearningType.vertical) {
            throw new FlowNodeException(
                    node,
                    ComponentType.VertFeaturePSI.getLabel() +
                            "只支持纵向联邦任务"
            );
        }

        boolean afterSegment = node.getParents().stream().anyMatch(x -> x.getComponentType() == ComponentType.Segment);
        if (!afterSegment) {
            throw new FlowNodeException(
                    node,
                    ComponentType.VertFeaturePSI.getLabel() +
                            "只能紧跟" +
                            ComponentType.Segment.getLabel() +
                            "之后执行，请调整您的流程。"
            );
        }

        // 限定只支持二分类的数据集
        // if (graph.getJob().getMyRole()==JobMemberRole.promoter) {
        //     DataIOComponent.Params dataIOParams = (DataIOComponent.Params) graph.findOneNodeFromParent(node, ComponentType.DataIO).getParamsModel();
        //
        // }

        graph.findOneNodeFromParent(node, ComponentType.DataIO);
    }

    @Override
    protected JSONObject createTaskParams(JobBuilder jobBuilder, FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws Exception {
        Member me = params.members.stream()
                .filter(x ->
                        CacheObjects.getMemberId().equals(x.memberId)
                                && graph.getJob().getMyRole() == x.memberRole
                )
                .findFirst()
                .orElse(null);

        return JObject.create()
                .append("bin_names", me.features)
                .append("method", params.method)
                .append("bin_num", params.count);
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.VertFeaturePSI;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) throws StatusCodeWithException {
        return taskResultService.findByTaskIdAndType(taskId, type);
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        // 数据切割输出了两个 DataSetInstance，所以这里在寻找输入时，需要加上名称限定，才能准确选择。
        return Arrays.asList(
                InputMatcher.of(
                        Names.Data.TRAIN_DATA_SET,
                        new OutputDataTypeAndNameOutputFilter(IODataType.DataSetInstance, Names.Data.TRAIN_DATA_SET)
                ),
                InputMatcher.of(
                        Names.Data.EVALUATION_DATA_SET,
                        new OutputDataTypeAndNameOutputFilter(IODataType.DataSetInstance, Names.Data.EVALUATION_DATA_SET)
                )
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(
                OutputItem.of(Names.JSON_RESULT, IODataType.Json)
        );
    }

    public static class Params extends AbstractCheckModel {

        @Check(require = true)
        private List<Member> members;
        @Check(require = true)
        private BinningComponent.BinningMethod method;
        @Check(require = true)
        private int count;

        //region getter/setter

        public List<Member> getMembers() {
            return members;
        }

        public void setMembers(List<Member> members) {
            this.members = members;
        }

        public BinningComponent.BinningMethod getMethod() {
            return method;
        }

        public void setMethod(BinningComponent.BinningMethod method) {
            this.method = method;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }


        //endregion
    }

    public static class Member extends AbstractCheckModel {
        @Check(require = true)
        private String memberId;
        @Check(require = true)
        private JobMemberRole memberRole;
        @Check(require = true)
        private List<String> features;

        // region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        public List<String> getFeatures() {
            return features;
        }

        public void setFeatures(List<String> features) {
            this.features = features;
        }


        //endregion

    }

}
