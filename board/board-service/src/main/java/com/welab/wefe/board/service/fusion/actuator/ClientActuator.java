package com.welab.wefe.data.fusion.service.actuator.board;

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
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.fusion.AlignApi;
import com.welab.wefe.board.service.api.fusion.DownBloomFilterApi;
import com.welab.wefe.board.service.fusion.manager.TaskResultManager;
import com.welab.wefe.board.service.service.DataSetService;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.fusion.FieldInfoService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.api.board.AlignApi;
import com.welab.wefe.data.fusion.service.api.board.GetBloomFilterApi;
import com.welab.wefe.data.fusion.service.manager.TaskResultManager;
import com.welab.wefe.data.fusion.service.service.FieldInfoService;
import com.welab.wefe.data.fusion.service.service.board.GatewayService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import com.welab.wefe.data.fusion.service.utils.primarykey.PrimaryKeyUtils;
import com.welab.wefe.fusion.core.actuator.psi.PsiClientActuator;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.utils.primarykey.FieldInfo;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public class ClientActuator extends PsiClientActuator {
    public List<String> columnList;

    /**
     * Fragment size, default 10000
     */
    private int shard_size = 1000;
    private int current_index = 0;
    public List<FieldInfo> fieldInfoList;

    public String memberId;

    public ClientActuator(String businessId, String dataSetId, Boolean isTrace, String traceColumn) {
        super(businessId, dataSetId, isTrace, traceColumn);
    }

    @Override
    public void init() throws StatusCodeWithException {
        FieldInfoService service = Launcher.CONTEXT.getBean(FieldInfoService.class);

        columnList = service.columnList(businessId);


        /**
         * Calculate the fragment size based on the number of fields
         */
        shard_size = shard_size / columnList.size();

        /**
         * Supplementary trace field
         */
        if (isTrace) {
            columnList.add(traceColumn);
        }

        /**
         * Find primary key composition fields
         */
        fieldInfoList = service.fieldInfoList(businessId);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public List<JObject> next() {

        long start = System.currentTimeMillis();

        DataSetService service = Launcher.CONTEXT.getBean(DataSetService.class);
        List<JObject> curList = Lists.newArrayList();
        try {
            curList = service.paging(columnList, dataSetId, current_index, shard_size);

        } catch (StatusCodeWithException e) {
        }

        LOG.info("cursor {} spend: {} curList {}", current_index, System.currentTimeMillis() - start, curList.size());

        current_index++;

        return curList;

        //TODO ck取数

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

        System.out.println("测试结果：" + JSON.toJSONString(fruit));
    }

    @Override
    public Boolean hasNext() {

        DataSetService service = Launcher.CONTEXT.getBean(DataSetService.class);
        List<JObject> curList = Lists.newArrayList();
//        try {
//      //      curList = service.paging(columnList, dataSetId, current_index, shard_size);
//
//        } catch (StatusCodeWithException e) {
//        }

        return curList.size() > 0;
    }

    @Override
    public PsiActuatorMeta downloadBloomFilter() {

        LOG.info("downloadBloomFilter start");

        //调用gateway
        GatewayService gatewayService = Launcher.CONTEXT.getBean(GatewayService.class);
        // ApiResult<?> test = gatewayService.callOtherMemberBoard(memberId, DownBloomFilterApi.class, new DownBloomFilterApi.Input(businessId));

        // LOG.info("downloadBloomFilter end {} ", test.data);

      //  return JObject.toJavaObject((JSONObject) test.data, PsiActuatorMeta.class);
    }

    @Override
    public byte[][] qureyFusionData(byte[][] bs) {

        LOG.info("qureyFusionData start");

        //调用gateway
        GatewayService gatewayService = Launcher.CONTEXT.getBean(GatewayService.class);
//        ApiResult<?> test = gatewayService.callOtherMemberBoard(memberId, AlignApi.class, new AlignApi.Input(businessId, bs));

        LOG.info("qureyFusionData start");
//        return (byte[][]) test.data;

        return null;
    }

    @Override
    public void sendFusionData(List<byte[]> rs) {
    }

    @Override
    public String hashValue(JObject value) {
//        return PrimaryKeyUtils.create(value, fieldInfoList);
        return value.getString("id");
    }
}
