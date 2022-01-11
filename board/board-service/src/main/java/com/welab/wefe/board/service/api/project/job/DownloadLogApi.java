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

import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author zane.luo
 */
@Api(path = "job/log/download", name = "download job log")
public class DownloadLogApi extends AbstractApi<DownloadLogApi.Input, ResponseEntity> {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<ResponseEntity> handle(DownloadLogApi.Input input) throws StatusCodeWithException {

        String url = globalConfigService.getFlowConfig().intranetBaseUri + "/job/download_logs?job_id=" + input.jobId;
        RequestEntity requestEntity = new RequestEntity<>(null, null, HttpMethod.GET, UrlUtil.createUri(url));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(requestEntity, byte[].class);
        return success(response);
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", require = true)
        private String jobId;

        //region getter/setter

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }


        //endregion
    }
}
