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

package com.welab.wefe.board.service.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.dto.AbstractDataIOParam;
import com.welab.wefe.board.service.component.base.dto.AbstractDataSetItem;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public class DataIOComponent extends AbstractComponent<DataIOComponent.Params> {

    @Autowired
    private TableDataSetService tableDataSetService;

    @Override
    public ComponentType taskType() {
        return ComponentType.DataIO;
    }

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        List<JobMemberMySqlModel> jobMembers = graph.getMembers();

        if (CollectionUtils.isEmpty(jobMembers) || jobMembers.size() < 2) {
            throw new FlowNodeException(node, "请至少为两个成员指定数据集");
        }

        if (CollectionUtils.isEmpty(params.getDataSetList()) || params.getDataSetList().size() < 2) {
            throw new FlowNodeException(node, "请选择多个数据集用于联邦");
        }

        if (jobMembers.stream().noneMatch(x -> x.getJobRole() == JobMemberRole.promoter)) {
            throw new FlowNodeException(node, "请为 promoter 指定数据集");
        }

        JobMemberMySqlModel promoter = jobMembers.stream().filter(
                        x -> x.getJobRole() == JobMemberRole.promoter && CacheObjects.getMemberId().equals(x.getMemberId()))
                .findFirst().orElse(null);

        if (params.getDataSetList().stream().noneMatch(x -> x.memberId.equals(promoter.getMemberId()))) {
            throw new FlowNodeException(node, "请为 promoter 指定数据集");
        }

        for (DataSetItem item : params.getDataSetList()) {
            if (CollectionUtils.isEmpty(item.features)) {
                throw new FlowNodeException(node, "请为 " + CacheObjects.getMemberName(item.memberId) + " 的数据集选择特征");
            }
        }

        DataSetItem promoterProjectDataSet = params.getDataSetList().stream().filter(x -> x.memberId.equals(promoter.getMemberId())).findFirst().orElse(null);
        TableDataSetMysqlModel promoterDataSet = tableDataSetService.findOneById(promoterProjectDataSet.dataSetId);
        if (!promoterDataSet.isContainsY()) {
            throw new FlowNodeException(node, "promoter 的数据集必须包含 y 值");
        }

        // Check if the data set has been deleted
        for (DataSetItem dataSet : params.getDataSetList()) {
            if (!CacheObjects.getMemberId().equals(dataSet.memberId)) {
                continue;
            }

            TableDataSetMysqlModel one = tableDataSetService.findOneById(dataSet.getDataSetId());
            if (one == null) {
                throw new FlowNodeException(node, "成员 " + CacheObjects.getMemberName(dataSet.memberId) + " 的数据集 " + dataSet.getDataSetId() + " 不存在，请检查是否已删除。");
            }
        }

        if (graph.getJob().getFederatedLearningType() == FederatedLearningType.horizontal) {
            List<DataSetItem> dataSetItems = params.getDataSetList();
            for (int i = 0; i < dataSetItems.size() - 1; i++) {
                if (!CollectionUtils.isEqualCollection(dataSetItems.get(i).getFeatures(), dataSetItems.get(i + 1).getFeatures())) {
                    throw new FlowNodeException(node, "横向建模需要保证所有样本所选特征列表一致。");
                }
            }
        }

        if (graph.getJob().getFederatedLearningType() == FederatedLearningType.mix) {
            List<DataSetItem> dataSetItems = params.getDataSetList().stream()
                    .filter(s -> s.getMemberRole() == JobMemberRole.promoter).collect(Collectors.toList());
            if (dataSetItems.size() < 2) {
                throw new FlowNodeException(node, "混合建模需要发起方数据集最少2个");
            }
            for (int i = 0; i < dataSetItems.size() - 1; i++) {
                if (!CollectionUtils.isEqualCollection(dataSetItems.get(i).getFeatures(),
                        dataSetItems.get(i + 1).getFeatures())) {
                    throw new FlowNodeException(node, "混合建模需要保证发起方样本所选特征列表一致。");
                }
            }
        }

    }


    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
            return null;
        }

        // Create the input parameters of the components in the kernel according to the component parameter settings in the interface
        DataSetItem myDataSetConfig = params.getDataSetList()
                .stream()
                .filter(x -> x.getMemberId().equals(CacheObjects.getMemberId()) && x.getMemberRole() == graph.getJob().getMyRole())
                .findFirst()
                .orElse(null);

        if (myDataSetConfig == null) {
            throw new FlowNodeException(node, "请保存自己的数据集信息。");
        }

        TableDataSetMysqlModel myDataSet = tableDataSetService.findOneById(myDataSetConfig.dataSetId);
        if (myDataSet == null) {
            throw new FlowNodeException(node, "找不到自己的数据集。");
        }

        JObject output = JObject
                .create()
                .append("data_set_id", myDataSet.getId())
                .append("with_label", myDataSet.isContainsY())
                .append("label_name", "y")
                .append("namespace", myDataSet.getStorageNamespace())
                .append("name", myDataSet.getStorageResourceName())
                .append("need_features", myDataSetConfig.features);

        return output;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel resultModel = null;
        JObject resultObj = JObject.create();

        TaskResultMySqlModel dataNormalResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.data_normal.name());
        if (dataNormalResult != null) {
            resultModel = dataNormalResult;
            JObject dataNormalObj = JObject.create(dataNormalResult.getResult());

            int tableCreateCount = dataNormalObj.getInteger("table_create_count");
            resultObj.append("table_create_count", tableCreateCount);
        }

        TaskResultMySqlModel modelResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.model_result.name());
        if (modelResult != null) {
            resultModel = modelResult;

            JObject modelResultObj = JObject.create(modelResult.getResult());
            JSONArray header = modelResultObj.getJObject("model_param").getJSONArray("header");
            resultObj.append("header", header);
        }
        if (resultModel != null) {
            resultModel.setResult(resultObj.toJSONString());
        }

        return resultModel;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {
        return null;
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {

        Params params = (Params) node.getParamsModel();

        // If no data set is selected
        if (CollectionUtils.isEmpty(params.dataSetList)) {
            return new ArrayList<>();
        }

        TableDataSetMysqlModel myDataSet = params.getMyDataSet();
        if (myDataSet == null) {
            throw new FlowNodeException(node, CacheObjects.getMemberName() + " 的数据集已被删除，不能加载已删除的数据集。");
        }

        return Arrays.asList(OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
    }

    public static class Params extends AbstractDataIOParam<DataSetItem> {

        /**
         * Find my data set object information from the configuration list
         */
        public TableDataSetMysqlModel getMyDataSet() {

            DataSetItem myDataSetConfig = getMyDataSetConfig();

            if (myDataSetConfig == null) {
                return null;
            }

            TableDataSetMysqlModel myDataSet = Launcher
                    .CONTEXT
                    .getBean(TableDataSetService.class)
                    .findOneById(myDataSetConfig.getDataSetId());

            return myDataSet;
        }

        /**
         * Find my data set configuration from the configuration list
         */
        public DataSetItem getMyDataSetConfig() {
            DataSetItem myDataSetConfig = Components
                    .getDataIOComponent()
                    .findMyData(dataSetList, x -> x.getMemberId());

            return myDataSetConfig;
        }


    }

    public static class DataSetItem extends AbstractDataSetItem {
        @Check(name = "选择的特征列")
        private List<String> features;

        //region getter/setter

        public List<String> getFeatures() {
            return features;
        }

        public void setFeatures(List<String> features) {
            this.features = features;
        }

        //endregion
    }


}
