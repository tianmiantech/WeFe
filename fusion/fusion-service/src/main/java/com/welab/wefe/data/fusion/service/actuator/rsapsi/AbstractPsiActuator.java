/**
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

package com.welab.wefe.data.fusion.service.actuator.rsapsi;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.data.fusion.service.actuator.AbstractActuator;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import com.welab.wefe.data.fusion.service.manager.TaskResultManager;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public abstract class AbstractPsiActuator extends AbstractActuator {

    protected String ip;
    protected int port;

    protected BloomFilters bf;

    public volatile PSIActuatorStatus status = PSIActuatorStatus.uninitialized;

    public List<JObject> fruit = new ArrayList<>();

    public volatile long lastLogTime = System.currentTimeMillis();

    public long socketTimeout = 1800000;


    public AbstractPsiActuator(String businessId, Integer dataCount) {
        super(businessId, dataCount);
    }

    @Override
    public void dump(List<JObject> fruit) {
        LOG.info("fruit insert ready...");

        if (fruit.isEmpty()) {
            return;
        }

        LOG.info("fruit inserting...");

        //Build table
        createTable(businessId, new ArrayList<>(fruit.get(0).keySet()));

        /**
         * Fruit Standard formatting
         */
        List<Map<String, Object>> fruits = fruit.
                stream().
                map(new Function<JObject, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> apply(JObject x) {
                        Map<String, Object> map = new LinkedHashMap();
                        for (Map.Entry<String, Object> column : x.entrySet()) {
                            map.put(column.getKey(), column.getValue());
                        }
                        return map;
                    }
                }).collect(Collectors.toList());

        TaskResultManager.saveTaskResultRows(businessId, fruits);

        LOG.info("fruit insert end...");
    }
}
