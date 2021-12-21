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

package com.welab.wefe.union.service.dto.common;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * service availability output
 *
 * @author aaron.li
 **/
public class ServiceAvailableOutput {
    /**
     * Service Name
     */
    private String service;
    /**
     * Whether it is successful (the value is true when the list of all services under it is true, otherwise it is false)
     */
    private boolean success;
    /**
     * describe message
     */
    private String message;

    /**
     * Corresponding service list
     */
    @JSONField(name = "list")
    private List<ServiceStatusOutput> serviceStatusOutputList;


    @Override
    public String toString() {
        return "{" +
                "service='" + service + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", serviceStatusOutputList=" + serviceStatusOutputList +
                '}';
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ServiceStatusOutput> getServiceStatusOutputList() {
        return serviceStatusOutputList;
    }

    public void setServiceStatusOutputList(List<ServiceStatusOutput> serviceStatusOutputList) {
        this.serviceStatusOutputList = serviceStatusOutputList;
    }
}
