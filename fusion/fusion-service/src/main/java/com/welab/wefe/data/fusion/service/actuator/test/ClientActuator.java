package com.welab.wefe.data.fusion.service.actuator.test;
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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.enums.TaskStatus;
import com.welab.wefe.data.fusion.service.manager.TaskResultManager;
import com.welab.wefe.data.fusion.service.service.FieldInfoService;
import com.welab.wefe.data.fusion.service.service.TaskService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import com.welab.wefe.data.fusion.service.utils.primarykey.PrimaryKeyUtils;
import com.welab.wefe.fusion.core.actuator.psi.PsiClientActuator;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.enums.ActionType;
import com.welab.wefe.fusion.core.enums.PSIActuatorStatus;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import com.welab.wefe.fusion.core.utils.SocketUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;
import org.apache.commons.compress.utils.Lists;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public class ClientActuator extends PsiClientActuator {

    protected String ip;
    protected int port;
    public List<String> columnList;

    /**
     * Fragment size, default 10000
     */
    private int shard_size = 1000;
    private int current_index = 0;
    public List<FieldInfo> fieldInfoList;

    public ClientActuator(String businessId, String ip, int port, String dataSetId, Boolean isTrace, String traceColumn) {
        super(businessId, dataSetId, isTrace, traceColumn);
        this.ip = ip;
        this.port = port;
//        this.columnList = columnList;
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

        //Notifies the server that no further action is required
        Socket socket = SocketUtils
                .create(ip, port)
                .setRetryCount(3)
                .builder();
        PSIUtils.sendString(socket, ActionType.end.name());
        SocketUtils.close(socket);

        TaskService taskService = Launcher.CONTEXT.getBean(TaskService.class);

        switch (status) {
            case success:
                taskService.updateByBusinessId(businessId, TaskStatus.Success, fusionCount.intValue(), getSpend());
                break;
            case falsify:
            case running:
                taskService.updateByBusinessId(businessId, TaskStatus.Interrupt, fusionCount.intValue(), getSpend());
                break;
            default:
                taskService.updateByBusinessId(businessId, TaskStatus.Failure, fusionCount.intValue(), getSpend());
                break;
        }
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

        //
//        List<JObject> curList = Lists.newArrayList();
//        for (int i = 1; i <= 100; i++) {
//            curList.add(JObject.create().append("id", i));
//        }
//
//        hasNext = false;

        return curList;
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
        try {
            curList = service.paging(columnList, dataSetId, current_index, shard_size);

        } catch (StatusCodeWithException e) {
        }

        return curList.size() > 0;
    }

    @Override
    public PsiActuatorMeta downloadBloomFilter() {
        Socket socket = null;
        try {
            LOG.info("Server@" + ip + ":" + port + " connecting!");
            socket = SocketUtils
                    .create(ip, port)
                    .setRetryCount(3)
                    .setRetryDelay(1000)
                    .builder();

            LOG.info("socket: {} ", socket);

            PSIUtils.sendString(socket, ActionType.download.name());

            LOG.info("client download bloom_filter data...");

            byte[][] pk = PSIUtils.receive2DBytes(socket);
            BigInteger e = PSIUtils.bytesToBigInteger(pk[0], 0, pk[0].length);
            BigInteger N = PSIUtils.bytesToBigInteger(pk[1], 0, pk[1].length);
            DataInputStream d_in = new DataInputStream(socket.getInputStream());
            int DB_size = (int) PSIUtils.receiveInteger(d_in);
            int bitSetSize = (int) PSIUtils.receiveInteger(d_in);
            byte[] b = PSIUtils.receiveBytes(socket);
            BitSet bs = BitSet.valueOf(b);

            BloomFilters bf = new BloomFilters(bitSetSize, DB_size, DB_size, bs);

            return PsiActuatorMeta.of(e, N, bf);
        } catch (IOException e1) {
            e1.printStackTrace();
            LOG.error(e1.getClass().getSimpleName() + " download bf error : " + e1.getMessage());
            //Interrupt tasks
            this.status = PSIActuatorStatus.exception;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public byte[][] qureyFusionData(byte[][] bs) {
        Socket socket = null;
        try {
            LOG.info("Server@" + ip + ":" + port + " connecting!");

            socket = SocketUtils
                    .create(ip, port)
                    .setRetryCount(3)
                    .builder();

            PSIUtils.sendString(socket, ActionType.align.name());

            PSIUtils.send2DBytes(socket, bs);

            return PSIUtils.receive2DBytes(socket);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void sendFusionData(List<byte[]> rs) {

        Socket socket = null;
        try {
            socket = SocketUtils
                    .create(ip, port)
                    .setRetryCount(3)
                    .builder();

            /**
             * Send alignment data to the server
             */
            PSIUtils.sendString(socket, ActionType.dump.name());

            ThreadUtil.sleep(1000);

            PSIUtils.send2DBytes(socket, rs);

            PSIUtils.receiveString(socket);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public String hashValue(JObject value) {
        return PrimaryKeyUtils.create(value, fieldInfoList);
//        return value.getString("id");
    }


    private boolean DUMP_TABLE_EXIST = false;

    public synchronized void createTable(String businessId, List<String> rows) {
        /*
         * Create a table if no table exists
         */
        if (!DUMP_TABLE_EXIST) {
            LOG.info("create fruit table...");
            TaskResultManager.createTaskResultTable(businessId, rows);
            DUMP_TABLE_EXIST = true;
        }
    }
}
