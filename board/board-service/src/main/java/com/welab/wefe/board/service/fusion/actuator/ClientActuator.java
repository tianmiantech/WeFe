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


import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.welab.wefe.board.service.api.project.fusion.actuator.psi.DownloadBFApi;
import com.welab.wefe.board.service.api.project.fusion.actuator.psi.PsiCryptoApi;
import com.welab.wefe.board.service.api.project.fusion.actuator.psi.ReceiveResultApi;
import com.welab.wefe.board.service.api.project.fusion.actuator.psi.ServerCloseApi;
import com.welab.wefe.board.service.api.project.fusion.actuator.psi.ServerSynStatusApi;
import com.welab.wefe.board.service.dto.fusion.PsiMeta;
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
import com.welab.wefe.fusion.core.enums.PSIActuatorStatus;

/**
 * @author hunter.zhao
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class ClientActuator extends AbstractPsiClientActuator {
    public Set<String> columnList;

    /**
     * Fragment size, default 200000
     */
    public int shardSize = 50000;
    public List<FieldInfo> fieldInfoList;
    public String dstMemberId;
    DataSetStorageService dataSetStorageService;
    GatewayService gatewayService = Launcher.getBean(GatewayService.class);

    private String[] headers;

    private final ReentrantLock lock = new ReentrantLock(true);

    public ClientActuator(String businessId, String dataSetId, Boolean isTrace, String traceColumn, String dstMemberId, Long dataCount) {
        super(businessId, dataSetId, isTrace, traceColumn, dataCount);
        this.dstMemberId = dstMemberId;
    }

    @Override
    public void init() throws Exception {
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
                fusionTaskService.updateErrorByBusinessId(
                        businessId,
                        FusionTaskStatus.Interrupt,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend(),
                        error
                );
                break;
            default:
                fusionTaskService.updateErrorByBusinessId(
                        businessId,
                        FusionTaskStatus.Failure,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend(),
                        error
                );
                break;
        }
    }

    @Override
    public void notifyServerClose() {
        //notify the server that the task has ended
        LOG.info("psi log, notify the server that the task has ended");
        try {
            gatewayService.callOtherMemberBoard(
                    dstMemberId,
                    ServerCloseApi.class,
                    new ServerCloseApi.Input(businessId, status.name(), error),
                    JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<JObject> getBucketByIndex(int index) {
        try {
            lock.lock();
            long start = System.currentTimeMillis();

            PageOutputModel model = dataSetStorageService.getListByPage(
                    Constant.DBName.WEFE_DATA,
                    dataSetStorageService.createRawDataSetTableName(dataSetId),
                    new PageInputModel(index, shardSize)
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


            LOG.info("psi log, cursor {} spend: {} curList {} list {}", index, System.currentTimeMillis() - start, curList.size(), list.size());


            return curList;

        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return null;
        } finally {
            lock.unlock();
        }

    }

    @Override
    public boolean isServerReady(String businessId) {

        try {
            JSONObject result = gatewayService.callOtherMemberBoard(
                    dstMemberId,
                    ServerSynStatusApi.class,
                    new ServerSynStatusApi.Input(businessId),
                    JSONObject.class
            );
            return result.getBoolean("ready");
        } catch (Exception e) {
            LOG.error("请求合作方失败！错误原因: {}", e.getMessage());
            status = PSIActuatorStatus.exception;
        }

        return false;
    }

    @Override
    public void dump(List<JObject> fruit) {
        LOG.info("psi log, fruit insert ready...");

        try {
            PsiDumpHelper.dump(businessId, columnList, fruit);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }

        LOG.info("psi log, fruit insert end...");
    }

    @Override
    public int bucketSize() {
        LOG.info("psi log, dataCount = " + dataCount.intValue());
        LOG.info("psi log, shardSize = " + shardSize);
        int bucketSize = dataCount.intValue() % shardSize == 0 ? dataCount.intValue() / shardSize
                : dataCount.intValue() / shardSize + 1;
        LOG.info("psi log, bucketSize = " + bucketSize);
        return bucketSize;
    }

    @Override
    public PsiActuatorMeta downloadActuatorMeta() throws StatusCodeWithException {

        LOG.info("psi log, downloadActuatorMeta start");

        //调用gateway
        JSONObject result = gatewayService.callOtherMemberBoard(
                dstMemberId,
                DownloadBFApi.class,
                new DownloadBFApi.Input(businessId),
                JSONObject.class
        );

        LOG.info("psi log, downloadBloomFilter end");

        PsiActuatorMeta meta = JObject.toJavaObject(result, PsiActuatorMeta.class);
        meta.setBfByDto(meta.getBfDto());
        return meta;
    }

    @Override
    public byte[][] dataTransform(byte[][] bs) throws StatusCodeWithException {
        LOG.info("psi log, dataTransform start");

        List<String> stringList = Lists.newArrayList();
        for (int i = 0; i < bs.length; i++) {
            stringList.add(Base64Util.encode(bs[i]));
        }

        PsiMeta result = gatewayService.callOtherMemberBoard(dstMemberId, PsiCryptoApi.class,
                new PsiCryptoApi.Input(businessId, stringList), PsiMeta.class);

        List<String> list = result.getBs();

        byte[][] ss = new byte[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            ss[i] = Base64Util.base64ToByteArray(list.get(i));
        }
        return ss;
    }

    @Override
    public void sendFusionDataToServer(List<JObject> rs) {
        List<String> stringList = rs.stream()
                .map(r -> Base64Util.encode(r.toString().getBytes()))
                .collect(Collectors.toList());

        try {
            gatewayService.callOtherMemberBoard(
                    dstMemberId,
                    ReceiveResultApi.class,
                    new ReceiveResultApi.Input(businessId, stringList)
            );
        } catch (Exception e) {
            LOG.error("sendFusionDataToServer error: ", e);
        }
    }

    @Override
    public String hashValue(JObject value) {
        return PrimaryKeyUtils.create(value, fieldInfoList);
    }
}
