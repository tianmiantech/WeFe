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
package com.welab.wefe.serving.service.api.orderstatistics;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.DateTypeEnum;
import com.welab.wefe.serving.service.service.OrderStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author ivenn.zheng
 * @date 2022/5/7
 */
@Api(path = "orderstatistics/download", name = "download order statistics")
public class DownloadApi extends AbstractApi<DownloadApi.Input, ResponseEntity<?>> {

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {
        return file(orderStatisticsService.downloadFile(input));
    }

    @Override
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


    public static class Input extends AbstractApiInput {

        @Check(name = "开始时间")
        private Date startTime;

        @Check(name = "结束时间")
        private Date endTime;

        @Check(name = "请求方 id")
        private String requestPartnerId;

        @Check(name = "请求方名称")
        private String requestPartnerName;

        @Check(name = "响应方 id")
        private String responsePartnerId;

        @Check(name = "响应方名称")
        private String responsePartnerName;

        @Check(name = "服务 id")
        private String serviceId;

        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "统计粒度", require = true)
        private String statisticalGranularity = DateTypeEnum.minute.name();

//        @Override
//        public void checkAndStandardize() throws StatusCodeWithException {
//            super.checkAndStandardize();
//
//            if (StringUtil.isEmpty(startTime.toString())) {
//                startTime = null;
//            }
//
//            if (StringUtil.isEmpty(endTime.toString())) {
//                endTime = null;
//            }
//
//        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public String getRequestPartnerId() {
            return requestPartnerId;
        }

        public void setRequestPartnerId(String requestPartnerId) {
            this.requestPartnerId = requestPartnerId;
        }

        public String getRequestPartnerName() {
            return requestPartnerName;
        }

        public void setRequestPartnerName(String requestPartnerName) {
            this.requestPartnerName = requestPartnerName;
        }

        public String getResponsePartnerId() {
            return responsePartnerId;
        }

        public void setResponsePartnerId(String responsePartnerId) {
            this.responsePartnerId = responsePartnerId;
        }

        public String getResponsePartnerName() {
            return responsePartnerName;
        }

        public void setResponsePartnerName(String responsePartnerName) {
            this.responsePartnerName = responsePartnerName;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getStatisticalGranularity() {
            return statisticalGranularity;
        }

        public void setStatisticalGranularity(String statisticalGranularity) {
            this.statisticalGranularity = statisticalGranularity;
        }
    }


}
