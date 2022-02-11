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

package com.welab.wefe.board.service.component.deep_learning;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.dto.AbstractDataIOParam;
import com.welab.wefe.board.service.component.base.dto.AbstractDataSetItem;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetSampleService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.AbstractImageDataSetParser;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
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
    @Autowired
    private ImageDataSetSampleService imageDataSetSampleService;

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

        if (graph.getJob().getMyRole() == JobMemberRole.promoter) {
            String labelList = null;
            // 检查数据集的有效性
            for (DataSetItem dataSetItem : params.getDataSetList()) {

                ImageDataSetOutputModel one = null;
                try {
                    one = imageDataSetService.findDataSetFromLocalOrUnion(dataSetItem.memberId, dataSetItem.dataSetId);
                } catch (StatusCodeWithException e) {
                    throw new FlowNodeException(node, e.getMessage());
                }
                if (one == null) {
                    throw new FlowNodeException(node, "成员 " + CacheObjects.getMemberName(dataSetItem.memberId) + " 的数据集 " + dataSetItem.getDataSetId() + " 不存在，请检查是否已删除。");
                }
                if (one.getLabeledCount() == 0) {
                    throw new FlowNodeException(node, "成员 " + CacheObjects.getMemberName(dataSetItem.memberId) + " 的数据集【" + one.getName() + "】已标注的样本量为 0，无法使用。");
                }
                // 检查各成员的数据集的标签列表是否一致
                if (labelList == null) {
                    labelList = StringUtil.join(one.getLabelSet(), ",");
                } else {
                    if (!labelList.equals(StringUtil.join(one.getLabelSet(), ","))) {
                        throw new FlowNodeException(node, "各成员提供的数据集标签列表不一致，无法创建任务。");
                    }
                }
            }
        }
    }


    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws Exception {
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


        // 生成数据集文件
        AbstractImageDataSetParser
                .getParser(myDataSet.getForJobType())
                .parseSamplesToDataSetFile(
                        graph.getJob().getJobId(),
                        myDataSet,
                        imageDataSetSampleService.allLabeled(myDataSetConfig.dataSetId),
                        params.trainTestSplitRatio
                );


        return output;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        return taskResultService.findByTaskIdAndType(taskId, type);
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

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (trainTestSplitRatio < 1 || trainTestSplitRatio > 99) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("数据集切割比例(训练:测试)，取值必须在 1-99 之间，当前取值：" + trainTestSplitRatio);
            }
        }

        public void fillDataSetDetail() throws StatusCodeWithException {

            ImageDataSetService imageDataSetService = Launcher.getBean(ImageDataSetService.class);

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
