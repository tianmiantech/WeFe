/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.dto.entity.DataIoTaskFeatureInfoOutputModel;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * Query the modeling feature column of the dataio task of the member according to jobid
 * <p>
 * Scenario: in OOT mode, the front end may need to view the modeling feature column of the original process dataio of
 * the corresponding member when selecting the dataset, and the front end can call this interface to query
 * </p>
 *
 * @author aaron.li
 **/
@Api(path = "project/flow/query/data_io_task_features", name = "Query the modeling feature column of the dataio task of the member according to jobid")
public class QueryDataIoTaskFeaturesApi extends AbstractApi<QueryDataIoTaskFeaturesApi.Input, QueryDataIoTaskFeaturesApi.Output> {
    @Autowired
    private TaskService taskService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        Output output = new Output();
        output.setDataIoTaskFeatureInfoList(taskService.findDataIoTaskFeatures(input));
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        /**
         * Process ID (non OOT mode)
         */
        private String flowId;
        /**
         * Job ID (OOT mode)
         */
        private String jobId;
        /**
         * The member ID to query. If it is blank, it means to query all members under the jobid
         */
        private String memberId;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }
    }

    public static class Output {
        private List<DataIoTaskFeatureInfoOutputModel> dataIoTaskFeatureInfoList;

        public List<DataIoTaskFeatureInfoOutputModel> getDataIoTaskFeatureInfoList() {
            return dataIoTaskFeatureInfoList;
        }

        public void setDataIoTaskFeatureInfoList(List<DataIoTaskFeatureInfoOutputModel> dataIoTaskFeatureInfoList) {
            this.dataIoTaskFeatureInfoList = dataIoTaskFeatureInfoList;
        }
    }

}
