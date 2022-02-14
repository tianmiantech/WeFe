/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.fusion.actuator;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.welab.wefe.board.service.api.project.fusion.actuator.psi.*;
import com.welab.wefe.board.service.dto.fusion.PsiMeta;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.board.service.service.fusion.FieldInfoService;
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
import com.welab.wefe.fusion.core.actuator.psi.AbstractPsiClientActuator;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;

import java.util.List;

/**
 * @author hunter.zhao
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class ClientActuator extends AbstractPsiClientActuator {
    public List<String> columnList;

    /**
     * Fragment size, default 10000
     */
    public int shardSize = 1000;
    public Integer currentIndex = 0;
    public List<FieldInfo> fieldInfoList;
    public String dstMemberId;
    DataSetStorageService dataSetStorageService;
    GatewayService gatewayService = Launcher.getBean(GatewayService.class);

    private String[] headers;
    public Boolean serverIsReady = false;

    public ClientActuator(String businessId, String dataSetId, Boolean isTrace, String traceColumn, String dstMemberId, Long dataCount) {
        super(businessId, dataSetId, isTrace, traceColumn, dataCount);
        this.dstMemberId = dstMemberId;
    }

    @Override
    public void init() throws StatusCodeWithException {
        FieldInfoService service = Launcher.getBean(FieldInfoService.class);

        columnList = service.columnList(businessId);


        /**
         * Calculate the fragment size based on the number of fields
         */
        shardSize = shardSize / columnList.size();

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
        headers = model.getV().toString().replace("\"", "").split(",");
    }


    @Override
    public void close() throws Exception {

        //remove Actuator
        ActuatorManager.remove(businessId);

        //update task status
        FusionTaskService fusionTaskService = Launcher.CONTEXT.getBean(FusionTaskService.class);
        switch (status) {
            case success:
                fusionTaskService.updateByBusinessId(
                        businessId,
                        FusionTaskStatus.Success,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend()
                );
                break;
            case falsify:
            case running:
                fusionTaskService.updateByBusinessId(
                        businessId,
                        FusionTaskStatus.Interrupt,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend()
                );
                break;
            default:
                fusionTaskService.updateByBusinessId(
                        businessId,
                        FusionTaskStatus.Failure,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend()
                );
                break;
        }
    }

    @Override
    public void notifyServerClose() {
        //notify the server that the task has ended
        try {
            gatewayService.callOtherMemberBoard(dstMemberId, ServerCloseApi.class, new ServerCloseApi.Input(businessId), JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<JObject> next() {
        synchronized (dataSetStorageService) {
            long start = System.currentTimeMillis();

            PageOutputModel model = dataSetStorageService.getListByPage(
                    Constant.DBName.WEFE_DATA,
                    dataSetStorageService.createRawDataSetTableName(dataSetId),
                    new PageInputModel(currentIndex, shardSize)
            );

            List<DataItemModel> list = model.getData();

            List<JObject> curList = Lists.newArrayList();
            list.forEach(x -> {
                String[] values = x.getV().toString().split(",");
                JObject jObject = JObject.create();
                for (int i = 0; i < headers.length; i++) {
                    if (columnList.contains(headers[i])) {
                        jObject.put(headers[i], values[i]);
                    }
                }
                curList.add(jObject);
            });


            LOG.info("cursor {} spend: {} curList {}", currentIndex, System.currentTimeMillis() - start, curList.size());

            currentIndex++;

            return curList;
        }
    }

    @Override
    public void dump(List<JObject> fruit) {
        LOG.info("fruit insert ready...");

        PsiDumpHelper.dump(businessId, columnList, fruit);

        LOG.info("fruit insert end...");

        System.out.println("测试结果：" + JSON.toJSONString(fruit));

        //记录进度
    }

    @Override
    public Boolean hasNext() {
        synchronized (dataSetStorageService) {
            PageOutputModel model = dataSetStorageService.getListByPage(
                    Constant.DBName.WEFE_DATA,
                    dataSetStorageService.createRawDataSetTableName(dataSetId),
                    new PageInputModel(currentIndex, shardSize)
            );
            return model.getData().size() > 0;
        }
    }

    @Override
    public PsiActuatorMeta downloadBloomFilter() throws StatusCodeWithException {

        LOG.info("downloadBloomFilter start");

        while (true) {
            if (serverIsReady) {
                break;
            }

            JSONObject result = gatewayService.callOtherMemberBoard(
                    dstMemberId,
                    ServerSynStatusApi.class,
                    new ServerSynStatusApi.Input(businessId),
                    JSONObject.class
            );
            serverIsReady = result.getBoolean("ready");
        }

        //调用gateway
        JSONObject result = gatewayService.callOtherMemberBoard(
                dstMemberId,
                DownloadBFApi.class,
                new DownloadBFApi.Input(businessId),
                JSONObject.class
        );


        LOG.info("downloadBloomFilter end {} ", result);

        PsiActuatorMeta meta = JObject.toJavaObject(result, PsiActuatorMeta.class);
        meta.setBfByDto(meta.getBfDto());
        return meta;
    }

    @Override
    public byte[][] queryFusionData(byte[][] bs) throws StatusCodeWithException {

        LOG.info("queryFusionData start");

        //调用gateway
        List<String> stringList = Lists.newArrayList();
        for (int i = 0; i < bs.length; i++) {
            stringList.add(Base64Util.encode(bs[i]));
        }

        PsiMeta result = gatewayService.callOtherMemberBoard(dstMemberId,
                PsiCryptoApi.class,
                new PsiCryptoApi.Input(businessId, stringList),
                PsiMeta.class
        );


        List<String> list = result.getBs();

        byte[][] ss = new byte[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            ss[i] = Base64Util.base64ToByteArray(list.get(i));
        }
        return ss;
    }

    @Override
    public void sendFusionData(List<byte[]> rs) {
        List<String> stringList = Lists.newArrayList();
        for (int i = 0; i < rs.size(); i++) {
            stringList.add(Base64Util.encode(rs.get(i)));
        }

        try {
            gatewayService.callOtherMemberBoard(
                    dstMemberId,
                    ReceiveResultApi.class,
                    new ReceiveResultApi.Input(businessId, stringList)
            );
        } catch (Exception e) {
            LOG.info("sendFusionData error: ", e);
            e.printStackTrace();
        }
    }


    @Override
    public String hashValue(JObject value) {
        return PrimaryKeyUtils.create(value, fieldInfoList);
    }
}
