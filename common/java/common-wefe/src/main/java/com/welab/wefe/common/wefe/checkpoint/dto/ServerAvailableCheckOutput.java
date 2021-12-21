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
public class ServerAvailableCheckOutput {
    public boolean available;
    public List<ServerCheckPointOutput> list;

    public ServerAvailableCheckOutput() {
    }

    public ServerAvailableCheckOutput(List<ServerCheckPointOutput> list) {
        this.list = list;
        if (list == null) {
            this.available = false;
        } else {
            this.available = list.stream().allMatch(x -> x.isSuccess());
        }

    }

    /**
     * 如果是其他成员访问服务状态，需要将 value 置空。
     */
    public void cleanValues() {
        if (list == null) {
            return;
        }

        for (ServerCheckPointOutput item : list) {
            item.setValue(null);
        }
    }
}
