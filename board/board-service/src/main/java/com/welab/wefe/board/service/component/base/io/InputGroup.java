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

package com.welab.wefe.board.service.component.base.io;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.JObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
public class InputGroup {
    private DataTypeGroup group;
    private List<NodeOutputItem> items;

    public InputGroup(DataTypeGroup group, List<NodeOutputItem> items) {
        this.group = group;
        this.items = items;
    }

    /**
     * splice the data structure required by the kernel
     */
    public JSONObject toJsonNode() {

        Map<String, List<NodeOutputItem>> groupByName = items
                .stream()
                .collect(
                        Collectors.groupingBy(
                                x -> x.getName(),
                                Collectors.toList()
                        )
                );

        Map<String, List<String>> map = new HashMap<>();
        groupByName.forEach((k, v) ->
                map.put(
                        k,
                        v
                                .stream()
                                .map(x -> x.getTaskName())
                                .collect(Collectors.toList())
                )
        );

        return JObject
                .create()
                .put(
                        group.getKey(),
                        map
                );
    }

    //region getter/setter

    public DataTypeGroup getGroup() {
        return group;
    }

    public void setGroup(DataTypeGroup group) {
        this.group = group;
    }

    public List<NodeOutputItem> getItems() {
        return items;
    }

    public void setItems(List<NodeOutputItem> items) {
        this.items = items;
    }

    //endregion
}
