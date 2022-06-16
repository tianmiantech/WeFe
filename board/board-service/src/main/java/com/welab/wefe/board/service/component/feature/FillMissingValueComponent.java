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
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.board.service.dto.kernel.machine_learning.KernelTask;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.model.JobBuilder;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lonnie
 */
@Service
public class FillMissingValueComponent extends AbstractComponent<FillMissingValueComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
    	DataIOComponent.Params dataIOParams = (DataIOComponent.Params) graph.findOneNodeFromParent(node, ComponentType.DataIO).getParamsModel();
        List<DataIOComponent.DataSetItem> dataSetItems = dataIOParams.getDataSetList();

        AtomicInteger count = new AtomicInteger();

        dataSetItems.forEach(x -> {
            params.getMembers().forEach(y -> {
                if (x.getMemberId().equals(y.getMemberId()) && x.getMemberRole() == y.getMemberRole()) {
                    count.addAndGet(1);
                }
            });
        });

        if (count.get() != dataSetItems.size()) {
            throw new FlowNodeException(node, "请保证当前节点所有成员都参与。");
        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.FillMissingValue;
    }

    @Override
    protected JSONObject createTaskParams(JobBuilder jobBuilder, FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        // Need to find DataIO data set
        FlowGraphNode dataIONode = graph.findOneNodeFromParent(node, ComponentType.DataIO);
        TaskMySqlModel dataIOTask = findTaskFromPretasks(preTasks, dataIONode);
        if (dataIONode == null || dataIOTask == null) {
            throw new FlowNodeException(node, "请添加DataIO组件!");
        }

        // Get the withLabel field in the dataIO node
        JObject taskConfig = JObject.create(dataIOTask.getTaskConf());
        if (taskConfig == null) {
            throw new FlowNodeException(node, "找不到DataIO_task中的with_label字段");
        }
        boolean withLabel = taskConfig.getBooleanValue("with_label");

        List<Params.Member> members = params.members;

        JObject featuresObj = JObject.create();
        for (Params.Member member : members) {

            if (CacheObjects.getMemberId().equals(member.memberId) && graph.getJobMemberIsMe().getJobRole() == member.memberRole) {
                List<Params.Feature> features = member.features;

                for (Params.Feature feature : features) {
                    String name = feature.name;
                    String method = feature.method;
                    double value = feature.count;

                    featuresObj.append(name, JObject.create().append("method", method)
                            .append("value", value));
                }
                break;
            }
        }
        JObject output = JObject.create()
                .append("features", featuresObj.toJSONString())
                .append("with_label", withLabel)
                .append("save_dataset", true);

        return output;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.data_fill_missing_value.name());

        if (taskResult == null) {
            return null;
        }

        TaskResultMySqlModel taskResultMySqlModel = new TaskResultMySqlModel();
        BeanUtils.copyProperties(taskResult, taskResultMySqlModel);
        JObject result = JObject.create(taskResult.getResult());

        List<JObject> members = result.getJSONList("members");
        members.forEach(member -> {
            String memberId = member.getString("member_id");
            String memberName = CacheObjects.getMemberName(memberId);
            member.append("member_name", memberName);
        });

        taskResultMySqlModel.setResult(JObject.create().append("members", members).toJSONString());

        return taskResultMySqlModel;
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

    @Override
    public KernelTask getTaskMembers(FlowGraph graph, FlowGraphNode node) {
        List<Member> members = new ArrayList<>();
        Params params = (Params) node.getParamsModel();

        if (CollectionUtils.isNotEmpty(params.getMembers())) {
            params.getMembers().forEach(x -> {
                Member member = ModelMapper.map(x, Member.class);
                member.setMemberName(CacheObjects.getMemberName(x.getMemberId()));
                members.add(member);
            });
        }

        return new KernelTask(members);
    }

    public static class Params extends AbstractCheckModel {

        private List<Strategy> strategies;

        public static class Strategy extends AbstractCheckModel {
            private String method;

            private double count;

            public String getMethod() {
                return method;
            }

            public void setMethod(String method) {
                this.method = method;
            }

            public double getCount() {
                return count;
            }

            public void setCount(double count) {
                this.count = count;
            }
        }

        private boolean saveDataset;

        @Check(require = true)
        private List<Member> members;

        public static class Member extends AbstractCheckModel {
            @Check(name = "成员id", require = true)
            private String memberId;

            @Check(name = "角色", require = true)
            private JobMemberRole memberRole;

            @Check(name = "特征信息", require = true)
            private List<Feature> features;

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

            public List<Feature> getFeatures() {
                return features;
            }

            public void setFeatures(List<Feature> features) {
                this.features = features;
            }
        }

        public static class Feature extends AbstractCheckModel {
            @Check(name = "填充方式", require = true)
            private String method;
            @Check(name = "特征名", require = true)
            private String name;
            @Check(name = "数值")
            private double count;

            public String getMethod() {
                return method;
            }

            public void setMethod(String method) {
                this.method = method;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public double getCount() {
                return count;
            }

            public void setCount(double count) {
                this.count = count;
            }
        }

        public boolean isSaveDataset() {
            return saveDataset;
        }

        public void setSaveDataset(boolean saveDataset) {
            this.saveDataset = saveDataset;
        }

        public List<Member> getMembers() {
            return members;
        }

        public void setMembers(List<Member> members) {
            this.members = members;
        }

        public List<Strategy> getStrategies() {
            return strategies;
        }

        public void setStrategies(List<Strategy> strategies) {
            this.strategies = strategies;
        }
    }

    @Override
    public boolean canSelectFeatures() {
        return true;
    }

}
