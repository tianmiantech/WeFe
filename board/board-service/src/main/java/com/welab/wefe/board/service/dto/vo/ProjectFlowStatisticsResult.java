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
package com.welab.wefe.board.service.dto.vo;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.wefe.enums.ProjectFlowStatisticsStatus;
import com.welab.wefe.common.wefe.enums.ProjectFlowStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zane
 * @date 2022/7/6
 */
public class ProjectFlowStatisticsResult {
    private Map<ProjectFlowStatisticsStatus, Integer> map = new HashMap<>();

    public String toJsonString() {
        return JSON.toJSONString(map);
    }

    public synchronized void put(ProjectFlowStatus status, int count) {
        ProjectFlowStatisticsStatus statisticsStatus = ProjectFlowStatisticsStatus.get(status);

        if (!map.containsKey(statisticsStatus)) {
            map.put(statisticsStatus, 0);
        }
        Integer oldCount = map.get(statisticsStatus);
        map.put(statisticsStatus, oldCount + count);
    }
}
