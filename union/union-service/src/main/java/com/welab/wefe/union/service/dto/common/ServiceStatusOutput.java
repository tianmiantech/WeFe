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

import com.welab.wefe.common.web.dto.AbstractApiOutput;

/**
 * service status availability output
 *
 * @author aaron.li
 **/
public class ServiceStatusOutput extends AbstractApiOutput {
    /**
     * Service Name
     */
    private String name;
    /**
     * Whether it is successful
     */
    private boolean success;

    private String message;

    private Long spend;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", spend=" + spend +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getSpend() {
        return spend;
    }

    public void setSpend(Long spend) {
        this.spend = spend;
    }
}
