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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.api.project.dataset.GetFeaturesApi;
import com.welab.wefe.board.service.component.base.dto.AbstractDataSetItem;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowNodeMySqlModel;
import com.welab.wefe.board.service.dto.vo.FlowDataSetOutputModel;
import com.welab.wefe.board.service.service.DataSetColumnService;
import com.welab.wefe.board.service.service.ProjectFlowJobService;
import com.welab.wefe.board.service.service.ProjectFlowNodeService;
import com.welab.wefe.board.service.service.ProjectFlowService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zane
 **/
@Api(path = "project/flow/table_data_set/list", name = "获取当前流程使用到的数据集列表")
public class ListFlowTableDataSetApi extends AbstractApi<ListFlowTableDataSetApi.Input, ListFlowTableDataSetApi.Output> {

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;
    @Autowired
    private ProjectFlowJobService projectFlowJobService;
    @Autowired
    private DataSetColumnService dataSetColumnService;
    @Autowired
    private ProjectFlowService projectFlowService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        ProjectFlowMySqlModel flow = projectFlowService.findOne(input.flowId);

        List<FlowDataSetOutputModel> list = new ArrayList<>();
        // 找到所有类似于 DataIO 的节点
        List<ProjectFlowNodeMySqlModel> nodes = projectFlowNodeService.listAboutLoadDataSetNodes(input.flowId);
        // 遍历节点，找到节点中选择的数据集，拼装数据集信息。
        for (ProjectFlowNodeMySqlModel node : nodes) {
            List<? extends AbstractDataSetItem> dataSetItemList = projectFlowJobService.listNodeDataSetItems(node);

            if (CollectionUtils.isEmpty(dataSetItemList)) {
                continue;
            }

            for (AbstractDataSetItem item : dataSetItemList) {
                boolean dataSetExisted = list.stream().anyMatch(x -> x.getMemberId().equals(item.getMemberId()) && x.getJobRole().equals(item.getMemberRole()) && x.getDataSetId().equals(item.getDataSetId()));

                if (dataSetExisted) {
                    continue;
                }

                FlowDataSetOutputModel dataSet = new FlowDataSetOutputModel();

                dataSet.setFeatures(dataSetColumnService.listProjectDataSetFeatures(new GetFeaturesApi.Input(node.getProjectId(), item.getMemberId(), item.getDataSetId())));
                dataSet.setProjectId(flow.getProjectId());
                dataSet.setFlowId(flow.getFlowId());
                dataSet.setJobRole(flow.getMyRole());
                dataSet.setMemberId(item.getMemberId());
                dataSet.setDataSetId(item.getDataSetId());

                list.add(dataSet);
            }

        }

        return success(new Output(list));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "flow id", require = true)
        public String flowId;
    }

    public static class Output extends AbstractApiOutput {

        public List<FlowDataSetOutputModel> list;

        public Output(List<FlowDataSetOutputModel> list) {
            this.list = list;
        }

        public Output() {

        }
    }
}
