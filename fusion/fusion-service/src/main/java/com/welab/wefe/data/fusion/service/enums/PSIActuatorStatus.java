/**
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

package com.welab.wefe.data.fusion.service.enums;

/**
 * @author hunter.zhao
 */
public enum PSIActuatorStatus {
    success("success", "成功"),

    uninitialized("uninitialized", "数据未初始化或初始化失败"),

    falsify("falsify", "数据验证不通过"),

    discard("discard", "任务被丢弃"),

    running("running", "运行"),

    exception("exception", "预料之外的情况");

    private String value;
    private String description;

    PSIActuatorStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String value() {
        return value;
    }

    public String description() {
        return description;
    }
}
