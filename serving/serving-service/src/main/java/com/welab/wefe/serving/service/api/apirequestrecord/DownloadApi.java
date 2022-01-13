/*
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
package com.welab.wefe.serving.service.api.apirequestrecord;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ApiRequestRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;

/**
 * @author ivenn.zheng
 * @date 2022/1/12
 */
@Api(path = "apirequestrecord/download", name = "download the api request records", login = false)
public class DownloadApi extends AbstractApi<DownloadApi.Input, ResponseEntity<?>> {


    @Autowired
    private ApiRequestRecordService apiRequestRecordService;


    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws StatusCodeWithException, IOException {
        apiRequestRecordService.downloadFile(input);
        return null;
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "service_id")
        private String serviceId;

        @Check(name = "client_id")
        private String clientId;

        @Check(name = "start_time")
        private long startTime;

        @Check(name = "end_time")
        private long endTime;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }

    protected ApiResult<ResponseEntity<?>> file(File file) throws StatusCodeWithException {
        if (!file.exists()) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("文件不存在：" + file.getAbsolutePath());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "public, max-age=3600");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Last-Modified", file.lastModified() + "");
        headers.add("ETag", String.valueOf(file.lastModified()));

        ResponseEntity<FileSystemResource> response = ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new FileSystemResource(file));

        ApiResult<ResponseEntity<?>> result = new ApiResult<>();
        result.data = response;
        return result;
    }



}
