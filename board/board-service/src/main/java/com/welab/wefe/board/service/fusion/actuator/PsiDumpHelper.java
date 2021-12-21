package com.welab.wefe.board.service.fusion.actuator;

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


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welab.wefe.board.service.service.fusion.FusionResultStorageService;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public class PsiDumpHelper {

    private static final FusionResultStorageService fusionResultStorageService;

    static {
        fusionResultStorageService = Launcher.CONTEXT.getBean(FusionResultStorageService.class);
    }

    private static void dumpHeaders(String businessId, List<String> headers) {
        //saveHeaderRow

        if (fusionResultStorageService.count(fusionResultStorageService.createRawDataSetHeaderTableName(businessId)) > 0) {
            return;
        }
        ;
        fusionResultStorageService.saveHeaderRow(businessId, headers);
    }

    public static void dump(String businessId, List<String> headers,List<JObject> fruit) {

        if (fruit.isEmpty()) {
            return;
        }

        dumpHeaders(businessId,headers);

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

        fusionResultStorageService.saveDataRows(businessId, fruits);

        System.out.println("测试结果：" + JSON.toJSONString(fruit));
    }
}
