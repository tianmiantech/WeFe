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

package com.welab.wefe.board.service.component.deep_learning;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.data_resource.image_data_set.ImageDataSetDownloadApi;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.board.service.dto.kernel.deep_learning.Env;
import com.welab.wefe.board.service.dto.kernel.deep_learning.KernelJob;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.model.JobBuilder;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.AbstractImageDataSetParser;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zane.luo
 */
public abstract class AbstractDeepLearningComponent extends AbstractComponent<AbstractDeepLearningComponent.Params> {

    @Autowired
    private ImageDataSetService imageDataSetService;

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        FlowGraphNode imageDataIo = graph.findOneNodeFromParent(node, ComponentType.ImageDataIO);
        if (imageDataIo == null) {
            throw new FlowNodeException(node, "尚未选择数据集");
        }
    }


    @Override
    protected JSONObject createTaskParams(JobBuilder jobBuilder, FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws StatusCodeWithException {
        ImageDataIOComponent.Params imageDataIoParam = (ImageDataIOComponent.Params) graph.findOneNodeFromParent(node, ComponentType.ImageDataIO).getParamsModel();

        Set<String> labelNames = new HashSet<>();
        for (ImageDataIOComponent.DataSetItem dataSetItem : imageDataIoParam.getDataSetList()) {
            ImageDataSetOutputModel dataSet = imageDataSetService.findDataSetFromLocalOrUnion(dataSetItem.getMemberId(), dataSetItem.getDataSetId());
            List<String> list = StringUtil.splitWithoutEmptyItem(dataSet.getLabelList(), ",");
            labelNames.addAll(list);
        }
        params.numClasses = labelNames.size();

        KernelJob job = new KernelJob();
        job.projectId = graph.getJob().getProjectId();
        job.jobId = graph.getJob().getJobId();
        job.taskId = node.createTaskId(graph.getJob());
        job.role = graph.getJob().getMyRole();
        job.memberId = CacheObjects.getMemberId();
        job.env = new Env(imageDataIoParam);
        job.members = Member.forDeepLearning(graph.getMembers());

        DataResourceOutputModel myJobDataSet = imageDataIoParam.getMyJobDataSet(job.role);
        JObject dataSetInfo = JObject.create(myJobDataSet);
        dataSetInfo.put("download_url", buildDataSetDownloadUrl(myJobDataSet.getId(), job.jobId, jobBuilder.dataSetVersion));
        dataSetInfo.put("download_file_name", AbstractImageDataSetParser.getDataSetFileName(job.jobId, jobBuilder.dataSetVersion));


        JObject output = JObject.create(job);
        output.put("data_set", dataSetInfo);
        output.put("algorithm_config", params);

        return output;
    }

    private String buildDataSetDownloadUrl(String dataSetId, String jobId, String version) {
        Api annotation = ImageDataSetDownloadApi.class.getAnnotation(Api.class);
        return Launcher.getBean(GlobalConfigService.class)
                .getBoardConfig()
                .intranetBaseUri
                + "/"
                + annotation.path()
                + "?data_set_id=" + dataSetId
                + "&job_id=" + jobId
                + "&version=" + version;

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

    public static class Params extends AbstractCheckModel {
        @Check(
                name = "算法类型",
                require = true,
                regex = "(paddle_clas|paddle_detection)",
                desc = "paddle_clas(分类), paddle_detection(目标检测)"
        )
        public String program;
        @Check(name = "迭代次数", require = true)
        public Integer maxIter;
        @Check(name = "聚合步长", require = true)
        public Integer innerStep;
        @Check(name = "检测模型名称", require = true)
        public String architecture;
        @Check(name = "类别数")
        public Integer numClasses;
        @Check(name = "学习率", require = true)
        public Double baseLr;
        @Check(name = "图像输入尺寸", require = true)
        public Integer[] imageShape;
        @Check(name = "批量大小", require = true)
        public Integer batchSize;
    }

}
