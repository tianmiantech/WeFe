/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.component.deep_learning;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.dto.AbstractDataIOParam;
import com.welab.wefe.board.service.component.base.dto.AbstractDataSetItem;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.data_set.ImageDataSetOutputModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.dataset.ImageDataSetService;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zane.luo
 */
@Service
public class ImageDataIOComponent extends AbstractComponent<ImageDataIOComponent.Params> {

    @Autowired
    private ImageDataSetService imageDataSetService;

    @Override
    public ComponentType taskType() {
        return ComponentType.ImageDataIO;
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

        // Check if the data set has been deleted
        for (DataSetItem dataSet : params.getDataSetList()) {
            if (!CacheObjects.getMemberId().equals(dataSet.memberId)) {
                continue;
            }

            ImageDataSetOutputModel one = null;
            try {
                one = imageDataSetService.findDataSetFromLocalOrUnion(dataSet.memberId, dataSet.dataSetId);
            } catch (StatusCodeWithException e) {
                throw new FlowNodeException(node, e.getMessage());
            }
            if (one == null) {
                throw new FlowNodeException(node, "成员 " + CacheObjects.getMemberName(dataSet.memberId) + " 的数据集 " + dataSet.getDataSetId() + " 不存在，请检查是否已删除。");
            }
            if (one.getLabeledCount() == 0) {
                throw new FlowNodeException(node, "成员 " + CacheObjects.getMemberName(dataSet.memberId) + " 的数据集【" + one.getName() + "】已标注的样本量为 0，无法使用。");
            }
        }

    }


    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        DataSetItem myDataSetConfig = params.getDataSetList()
                .stream()
                .filter(x ->
                        x.getMemberId().equals(CacheObjects.getMemberId())
                                && x.getMemberRole() == graph.getJob().getMyRole()
                )
                .findFirst()
                .orElse(null);

        ImageDataSetMysqlModel myDataSet = imageDataSetService.findOneById(myDataSetConfig.dataSetId);

        JObject output = JObject.create(myDataSet);

        return output;
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
        return null;
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return null;
    }

    public static class Params extends AbstractDataIOParam<DataSetItem> {
        @Check(name = "数据集切割比例", desc = "取值1-99，该值为训练集的百分比。", require = true)
        public int trainTestSplitRatio;

        public void fillDataSetDetail() throws StatusCodeWithException {

            ImageDataSetService imageDataSetService = Launcher.CONTEXT.getBean(ImageDataSetService.class);

            for (ImageDataIOComponent.DataSetItem dataSetItem : dataSetList) {
                dataSetItem.dataSet = imageDataSetService.findDataSetFromLocalOrUnion(dataSetItem.memberId, dataSetItem.dataSetId);
            }
        }
    }

    public static class DataSetItem extends AbstractDataSetItem {
        @Check(desc = "非入参，而是当此对象作为返回值时输出的字段。")
        public ImageDataSetOutputModel dataSet;
    }


}
