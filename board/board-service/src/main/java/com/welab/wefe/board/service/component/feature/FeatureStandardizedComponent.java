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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data standardization component
 *
 * @author lonnie
 */
@Service
public class FeatureStandardizedComponent extends AbstractComponent<FeatureStandardizedComponent.Params> {
    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
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

        List<MemberFeatureInfoModel> members = params.getMembers();
        JObject output = JObject.create();
        for (MemberFeatureInfoModel member : members) {
            if (CacheObjects.getMemberId().equals(member.getMemberId())) {
                List<MemberFeatureInfoModel.Feature> features = member.getFeatures();
                List<String> fields = new ArrayList<>();
                features.forEach(x -> {
                    if (StringUtil.isNotEmpty(x.getMethod())) {
                        fields.add(x.getName());
                    }
                });

                output.append("fields", fields);

                break;
            }
        }

        output
                .append("with_label", withLabel)
                .append("save_dataset", true);


        return output;
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.FeatureStandardized;
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

        @Check(name = "标准化方法", desc = "'z-score'/'min-max' 标准化方法，默认z-score")
        private String method;

        @Check(name = "成员信息")
        private List<MemberFeatureInfoModel> members;

        @Check(name = "是否保存")
        private boolean saveDataset;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<MemberFeatureInfoModel> getMembers() {
            return members;
        }

        public void setMembers(List<MemberFeatureInfoModel> members) {
            this.members = members;
        }

        public boolean isSaveDataset() {
            return saveDataset;
        }

        public void setSaveDataset(boolean saveDataset) {
            this.saveDataset = saveDataset;
        }
    }

    @Override
    public boolean canSelectFeatures() {
        return true;
    }

}
