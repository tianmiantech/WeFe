package com.welab.wefe.board.service.fusion.actuator.psi;

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


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.board.service.service.fusion.FusionResultStorageService;
import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.fusion.core.actuator.psi.PsiServerActuator;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public class ServerActuator extends PsiServerActuator {
    public ServerActuator(String businessId, BloomFilters bloomFilters, BigInteger N, BigInteger e, BigInteger d) {
        super(businessId, bloomFilters, N, e, d);
    }

    @Override
    public void dump(List<JObject> fruit) {
        LOG.info("fruit insert ready...");

        if (fruit.isEmpty()) {
            return;
        }

        LOG.info("fruit inserting...");

        /**
         * Fruit Standard formatting
         */
        List<List<Object>> fruits = fruit.
                stream().
                map(new Function<JObject, List<Object>>() {
                    @Override
                    public List<Object> apply(JObject x) {
                        List<Object> obj = Lists.newArrayList();
                        for (Map.Entry<String, Object> column : x.entrySet()) {
                            obj.add(column.getValue());
                        }
                        return obj;
                    }
                }).collect(Collectors.toList());

        FusionResultStorageService fusionResultStorageService = Launcher.CONTEXT.getBean(FusionResultStorageService.class);
        fusionResultStorageService.saveDataRows(businessId, fruits);

        LOG.info("fruit insert end...");

        System.out.println("测试结果：" + JSON.toJSONString(fruit));
    }

    @Override
    public void close() throws Exception {
        //remove Actuator
        ActuatorManager.remove(businessId);

        //update task status
        FusionTaskService fusionTaskService = Launcher.CONTEXT.getBean(FusionTaskService.class);
        fusionTaskService.updateByBusinessId(businessId, FusionTaskStatus.Success, fusionCount.intValue(), getSpend());
    }
}
