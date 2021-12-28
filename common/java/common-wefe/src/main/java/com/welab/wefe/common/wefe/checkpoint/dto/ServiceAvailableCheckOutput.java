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

import java.util.List;

/**
 * @author zane
 * @date 2021/12/16
 */
public class ServiceAvailableCheckOutput {
    public boolean available;
    public String message;
    public List<ServiceCheckPointOutput> list;

    public ServiceAvailableCheckOutput() {
    }

    public ServiceAvailableCheckOutput(List<ServiceCheckPointOutput> list) {
        this.list = list;
        if (list == null) {
            this.available = false;
        } else {
            ServiceCheckPointOutput failedCheckpoint = list.stream()
                    .filter(x -> !x.isSuccess())
                    .findAny()
                    .orElse(null);

            if (failedCheckpoint == null) {
                this.available = true;
            } else {
                this.available = false;
                this.message = failedCheckpoint.getMessage();
            }
        }
    }

    public ServiceAvailableCheckOutput(String message) {
        this.message = message;
    }

    /**
     * 如果是其他成员访问服务状态，需要将 value 置空。
     */
    public void cleanValues() {
        if (list == null) {
            return;
        }

        for (ServiceCheckPointOutput item : list) {
            item.setValue(null);
        }
    }
}
