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
package com.welab.wefe.serving.service.api.serviceorder;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.service.ServiceOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.Date;

/**
 * @author ivenn.zheng
 * @date 2022/5/5
 */
@Api(path = "serviceorder/download", name = "download service orders")
public class DownloadApi extends AbstractApi<DownloadApi.Input, ResponseEntity<?>> {

    @Autowired
    private ServiceOrderService serviceOrderService;


    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {
        return file(serviceOrderService.downloadFile(input));
    }

    public static class Input extends AbstractApiInput {


        @Check(name = "订单 id")
        private String id;

        @Check(name = "服务 id")
        private String serviceId;

        @Check(name = "服务名称")
        private String serviceName;

        @Check(name = "服务类型")
        private String serviceType;

        @Check(name = "订单类型") // 是否为己方生成的订单;1 是, 0否
        private Integer orderType;

        @Check(name = "订单状态")  // 订单状态;成功、失败、进行中
        private String status;

        @Check(name = "请求方名称")
        private String requestPartnerName;

        @Check(name = "响应方名称")
        private String responsePartnerName;

        @Check(name = "开始时间")
        private Date startTime;

        @Check(name = "结束时间")
        private Date endTime;

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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getOrderType() {
            return orderType;
        }

        public void setOrderType(Integer orderType) {
            this.orderType = orderType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

//        public String getRequestPartnerId() {
//            return requestPartnerId;
//        }
//
//        public void setRequestPartnerId(String requestPartnerId) {
//            this.requestPartnerId = requestPartnerId;
//        }

        public String getRequestPartnerName() {
            return requestPartnerName;
        }

        public void setRequestPartnerName(String requestPartnerName) {
            this.requestPartnerName = requestPartnerName;
        }

//        public String getResponsePartnerId() {
//            return responsePartnerId;
//        }
//
//        public void setResponsePartnerId(String responsePartnerId) {
//            this.responsePartnerId = responsePartnerId;
//        }

        public String getResponsePartnerName() {
            return responsePartnerName;
        }

        public void setResponsePartnerName(String responsePartnerName) {
            this.responsePartnerName = responsePartnerName;
        }

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }
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

}
