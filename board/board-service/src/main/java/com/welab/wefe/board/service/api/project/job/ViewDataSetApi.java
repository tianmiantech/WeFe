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

package com.welab.wefe.board.service.api.project.job;

import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "job/data_set/view", name = "view data set data rows", login = false)
public class ViewDataSetApi extends AbstractApi<ViewDataSetApi.Input, ResponseEntity> {

    @Autowired
    private TaskResultService taskResultService;
    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<ResponseEntity> handle(ViewDataSetApi.Input input) throws StatusCodeWithException {
        List<TaskResultMySqlModel> list = taskResultService.findList(input.jobId, input.nodeId, input.memberRole,
                "data_normal");
        if (!list.isEmpty()) {
            TaskResultMySqlModel one = list.get(0);
            JObject root = JObject.create(one.getResult());
            String tableName = root.getString("table_name");
            String tableNamespace = root.getString("table_namespace");
            String url = globalConfigService.getFlowConfig().intranetBaseUri
                    + String.format("/data_set/view?table_name=%s&table_namespace=%s", tableName, tableNamespace);
            RequestEntity requestEntity = new RequestEntity<>(null, null, HttpMethod.GET, UrlUtil.createUri(url));
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> response = restTemplate.exchange(requestEntity, Object.class);
            return success(response);
        }
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", require = true)
        private String jobId;

        @Check(name = "节点Id", require = true)
        private String nodeId;

        @Check(name = "角色", require = true)
        private JobMemberRole memberRole;

        //region getter/setter

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        //endregion
    }
}
