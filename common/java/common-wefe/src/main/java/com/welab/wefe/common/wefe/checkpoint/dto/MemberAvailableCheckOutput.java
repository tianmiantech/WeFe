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
package com.welab.wefe.common.wefe.checkpoint.dto;

import com.welab.wefe.common.wefe.enums.ServiceType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zane
 * @date 2021/12/16
 */
public class MemberAvailableCheckOutput {
    public boolean available = true;
    public ServiceType errorServiceType;
    public String message;
    public Map<ServiceType, ServiceAvailableCheckOutput> details = new LinkedHashMap<>();

    public MemberAvailableCheckOutput() {
    }

    public void put(ServiceType serviceType, ServiceAvailableCheckOutput item) {
        details.put(serviceType, item);
        if (!item.available) {
            errorServiceType = serviceType;
            available = false;
            message = item.message;
        }
    }
}
