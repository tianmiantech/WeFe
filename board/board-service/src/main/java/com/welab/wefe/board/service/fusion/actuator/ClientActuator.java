package com.welab.wefe.board.service.fusion.actuator;

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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.welab.wefe.board.service.api.fusion.actuator.psi.DownloadBFApi;
import com.welab.wefe.board.service.api.fusion.actuator.psi.PsiCryptoApi;
import com.welab.wefe.board.service.exception.MemberGatewayException;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.fusion.FieldInfoService;
import com.welab.wefe.board.service.service.fusion.FusionResultStorageService;
import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.board.service.util.primarykey.FieldInfo;
import com.welab.wefe.board.service.util.primarykey.PrimaryKeyUtils;
import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.model.PageInputModel;
import com.welab.wefe.common.data.storage.model.PageOutputModel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.fusion.core.actuator.psi.PsiClientActuator;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;

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
    public String dstMemberId;
    DataSetStorageService dataSetStorageService;
    FusionResultStorageService fusionResultStorageService;

    private String[] headers;

    public ClientActuator(String businessId, String dataSetId, Boolean isTrace, String traceColumn, String dstMemberId) {
        super(businessId, dataSetId, isTrace, traceColumn);
        this.dstMemberId = dstMemberId;
    }

    @Override
    public void init() throws StatusCodeWithException {
        FieldInfoService service = Launcher.getBean(FieldInfoService.class);

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

        /**
         * Initialize dataset header
         */
        dataSetStorageService = Launcher.CONTEXT.getBean(DataSetStorageService.class);
        DataItemModel model = dataSetStorageService.getByKey(
                Constant.DBName.WEFE_DATA,
                dataSetStorageService.createRawDataSetTableName(dataSetId) + ".meta",
                "header"
        );
        headers = model.getV().toString().split(",");
    }


    @Override
    public void close() throws Exception {
        //remove Actuator
        ActuatorManager.remove(businessId);

        //update task status
        FusionTaskService fusionTaskService = Launcher.CONTEXT.getBean(FusionTaskService.class);
        fusionTaskService.updateByBusinessId(businessId, FusionTaskStatus.Success, fusionCount.intValue(), getSpend());
    }

    @Override
    public void notifyServerClose() {
        //notify the server that the task has ended

    }

    @Override
    public synchronized List<JObject> next() {
        long start = System.currentTimeMillis();

        PageOutputModel model = dataSetStorageService.getListByPage(
                Constant.DBName.WEFE_DATA,
                dataSetStorageService.createRawDataSetTableName(dataSetId),
                new PageInputModel(current_index, shard_size)
        );

        List<DataItemModel> list = model.getData();

        List<JObject> curList = Lists.newArrayList();
        list.forEach(x -> {
            String[] values = x.getV().toString().split(",");
            JObject jObject = JObject.create();
            for (int i = 0; i < headers.length; i++) {
                jObject.put(headers[i], values[i]);
            }
            curList.add(jObject);
        });


        LOG.info("cursor {} spend: {} curList {}", current_index, System.currentTimeMillis() - start, curList.size());

        current_index++;

        return curList;
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

        fusionResultStorageService.saveDataRows(businessId, fruits);

        LOG.info("fruit insert end...");

        System.out.println("测试结果：" + JSON.toJSONString(fruit));
    }

    @Override
    public Boolean hasNext() {
        if (dataSetStorageService == null) {
            dataSetStorageService = Launcher.CONTEXT.getBean(DataSetStorageService.class);
        }

        synchronized (dataSetStorageService) {
            PageOutputModel model = dataSetStorageService.getListByPage(
                    Constant.DBName.WEFE_DATA,
                    dataSetStorageService.createRawDataSetTableName(dataSetId),
                    new PageInputModel(current_index, shard_size)
            );
            return model.getData().size() > 0;
        }
    }

    @Override
    public PsiActuatorMeta downloadBloomFilter() {

        LOG.info("downloadBloomFilter start");

        //调用gateway
        GatewayService gatewayService = Launcher.getBean(GatewayService.class);
        JSONObject result = null;
        try {
            result = gatewayService.callOtherMemberBoard(dstMemberId, DownloadBFApi.class, new DownloadBFApi.Input(businessId), JSONObject.class);
        } catch (MemberGatewayException e) {
            e.printStackTrace();
        }

        LOG.info("downloadBloomFilter end {} ", result);

        return JObject.toJavaObject(result, PsiActuatorMeta.class);
    }

    @Override
    public byte[][] queryFusionData(byte[][] bs) {

        LOG.info("queryFusionData start");

        //调用gateway
        GatewayService gatewayService = Launcher.getBean(GatewayService.class);
        List<String> stringList = Lists.newArrayList();
        for (int i = 0; i < bs.length; i++) {
            stringList.add(Base64Util.encode(bs[i]));
        }
        ApiResult<JSONObject> result = null;
        try {
            result = gatewayService.callOtherMemberBoard(dstMemberId, "fusion/psi/handle", JObject.create(new PsiCryptoApi.Input(businessId, stringList)));
        } catch (MemberGatewayException e) {
            LOG.info("error: {}", e);
            e.printStackTrace();
        }

        JSONArray response = result.data.getJSONArray("bytes");

        byte[][] ss = new byte[response.size()][];
        for (int i = 0; i < response.size(); i++) {
            ss[i] = Base64Util.base64ToByteArray(response.getString(i));
        }

        LOG.info("qureyFusionData end,{}", JSON.toJSONString(ss));
        return ss;
    }

    @Override
    public void sendFusionData(List<byte[]> rs) {

    }


    @Override
    public String hashValue(JObject value) {
        return PrimaryKeyUtils.create(value, fieldInfoList);
    }
}
