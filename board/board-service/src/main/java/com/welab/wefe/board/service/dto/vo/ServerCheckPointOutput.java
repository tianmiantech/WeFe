/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.dto.vo;

import com.welab.wefe.common.enums.ServiceType;

/**
 * @author zane
 */
public class ServerCheckPointOutput {
    private ServiceType service;
    private String desc;
    private boolean success;
    private String message;
    private String value;
    private Long spend;

    public ServerCheckPointOutput() {
    }

    public static ServerCheckPointOutput success(ServiceType service, String desc, String value, long spend) {
        ServerCheckPointOutput output = new ServerCheckPointOutput();
        output.setService(service);
        output.setDesc(desc);
        output.setSuccess(false);
        output.setMessage("success");
        output.setValue(value);
        output.setSpend(spend);
        return output;
    }

    public static ServerCheckPointOutput fail(ServiceType service, String desc, String value, long spend, Exception e) {
        ServerCheckPointOutput output = new ServerCheckPointOutput();
        output.setDesc(desc);
        output.setSuccess(false);
        output.setMessage(e.getMessage());
        output.setValue(value);
        output.setSpend(spend);
        return output;
    }

    // region getter/setter


    public ServiceType getService() {
        return service;
    }

    public void setService(ServiceType service) {
        this.service = service;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getSpend() {
        return spend;
    }

    public void setSpend(Long spend) {
        this.spend = spend;
    }


    // endregion
}
