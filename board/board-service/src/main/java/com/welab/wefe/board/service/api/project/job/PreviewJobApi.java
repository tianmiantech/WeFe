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

package com.welab.wefe.board.service.api.project.job;

import com.welab.wefe.board.service.dto.entity.job.PreviewJobNodeOutputModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.service.JobService;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Api(path = "project/flow/job/preview", name = "Preview flow execution process")
public class PreviewJobApi extends AbstractApi<PreviewJobApi.Input, PreviewJobApi.Output> {

    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        FlowGraph graph = jobService.createFlowGraph(input.flowId);
        jobService.setGraphHasCacheResult(graph, input.useCache);

        List<PreviewJobNodeOutputModel> nodes = graph
                .getJobSteps(input.endNodeId)
                .stream()
                .map(x -> {
                    PreviewJobNodeOutputModel model = ModelMapper.map(x, PreviewJobNodeOutputModel.class);

                    // Guess its input node
                    try {
                        model.input = x.getComponent().getInputs(graph, x);
                        model.output = x.getComponent().getOutputs(graph, x);

                    } catch (FlowNodeException e) {
                        System.out.println(e.getMessage());
                    }

                    return model;
                })
                .collect(Collectors.toList());

        long hasCacheResultCount = nodes.stream().filter(x -> x.getHasCacheResult()).count();
        long noCacheResultCount = nodes.stream().filter(x -> !x.getHasCacheResult()).count();

        return success(new Output(hasCacheResultCount, noCacheResultCount, nodes));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "流程主键", require = true)
        private String flowId;
        @Check(name = "是否使用缓存", require = true)
        private boolean useCache = true;
        @Check(name = "终止节点", desc = "为空时表示执行到流程最后")
        private String endNodeId;

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public boolean isUseCache() {
            return useCache;
        }

        public void setUseCache(boolean useCache) {
            this.useCache = useCache;
        }

        public String getEndNodeId() {
            return endNodeId;
        }

        public void setEndNodeId(String endNodeId) {
            this.endNodeId = endNodeId;
        }
    }

    public static class Output {
        private long hasCacheResultCount;
        private long noCacheResultCount;
        private List<PreviewJobNodeOutputModel> list;

        public Output(long hasCacheResultCount, long noCacheResultCount, List<PreviewJobNodeOutputModel> list) {
            this.hasCacheResultCount = hasCacheResultCount;
            this.noCacheResultCount = noCacheResultCount;
            this.list = list;
        }

        //region getter/setter


        public long getHasCacheResultCount() {
            return hasCacheResultCount;
        }

        public void setHasCacheResultCount(long hasCacheResultCount) {
            this.hasCacheResultCount = hasCacheResultCount;
        }

        public long getNoCacheResultCount() {
            return noCacheResultCount;
        }

        public void setNoCacheResultCount(long noCacheResultCount) {
            this.noCacheResultCount = noCacheResultCount;
        }

        public List<PreviewJobNodeOutputModel> getList() {
            return list;
        }

        public void setList(List<PreviewJobNodeOutputModel> list) {
            this.list = list;
        }


        //endregion
    }
}
