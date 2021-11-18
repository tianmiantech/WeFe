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
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.enums.TaskStatus;
import com.welab.wefe.data.fusion.service.manager.TaskManager;
import com.welab.wefe.data.fusion.service.manager.TaskResultManager;
import com.welab.wefe.data.fusion.service.service.TaskService;
import com.welab.wefe.fusion.core.actuator.psi.PsiServerActuator;
import com.welab.wefe.fusion.core.enums.ActionType;
import com.welab.wefe.fusion.core.enums.PSIActuatorStatus;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.FusionThreadPool;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
public class ServerActuator extends PsiServerActuator {

    private ServerSocket serverSocket;

    protected String ip;
    protected int port;

    public ServerActuator(String businessId, BloomFilters bloomFilters, String ip, int port, BigInteger N, BigInteger e, BigInteger d) {
        super(businessId, bloomFilters, N, e, d);
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void init() throws StatusCodeWithException {
        FusionThreadPool.run(() -> listen());
    }

    @Override
    public void dump(List<JObject> fruit) {

//            //Put in storage
//            dump(fruit);

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

        System.out.println("server 测试结果：" + JSON.toJSONString(fruit));
    }

    @Override
    public void close() throws Exception {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            LOG.warn(e.getClass().getSimpleName() + " close error:" + e.getMessage());
        }


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

//        TaskManager.remove(businessId);

    }

    private void listen() {

        try {
            serverSocket = new ServerSocket(port);
            LOG.info("Server@" + InetAddress.getLocalHost() + " start!");
        } catch (IOException e) {
            LOG.error(e.getClass().getSimpleName() + "server socket start error:" + e.getMessage());
        }

        LOG.info("PsiServerActuator listening...");

        try {
            while (true) {
                // listen PORT;
                Socket socket = serverSocket.accept();
                CommonThreadPool.run(() -> execute(socket));
            }
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " PsiServerActuator listen  error:" + e.getMessage());
        } finally {
        }
    }


    private void execute(Socket socket) {
        String action = PSIUtils.receiveString(socket);
        LOG.info("执行execute：" + action);
        if (ActionType.download.name().equals(action)) {
            /**
             * download
             */
            sendBloomFilter(socket);
        } else if (ActionType.align.name().equals(action)) {
            align(socket);
        } else if (ActionType.dump.name().equals(action)) {
            dump(socket);
        } else if (ActionType.end.name().equals(action)) {
            end();
        }

        //Update last time
//        lastLogTime = System.currentTimeMillis();
    }


    private void end() {
        //Modify the state of
        this.status = PSIActuatorStatus.success;

        LOG.info("align end...");
    }


    private void sendBloomFilter(Socket socket) {

        LOG.info("server send bloomfilter data...");
        try {
            DataOutputStream d_out = new DataOutputStream(socket.getOutputStream());

            byte[][] ret = new byte[2][];
            ret[0] = PSIUtils.bigIntegerToBytes(e, false);
            ret[1] = PSIUtils.bigIntegerToBytes(N, false);

            PSIUtils.send2DBytes(socket, ret);
            PSIUtils.sendInteger(d_out, bf.count());
            PSIUtils.sendInteger(d_out, bf.size());
            PSIUtils.sendBytes(socket, bf.getBitSet().toByteArray());
            System.out.println(bf.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void align(Socket socket) {

        LOG.info("align start...");

        long start = System.currentTimeMillis();

        try {

            byte[][] query = PSIUtils.receive2DBytes(socket);
            //   DataInputStream d_in = new DataInputStream(socket.getInputStream());
            // int index = (int) PSIUtils.receiveInteger(d_in);

            LOG.info("server wait spend :  {} ms ", (System.currentTimeMillis() - start));

            long start1 = System.currentTimeMillis();

            //Encrypted again
            byte[][] result = CryptoUtils.sign(N, d, query);

            LOG.info("server a.mod(N) spend :  {} ms size: {}", (System.currentTimeMillis() - start1), result.length);

            /**
             * Return the query result
             */
            PSIUtils.send2DBytes(socket, result);

            processedCount.add(result.length);
            LOG.info("processedCount: " + processedCount.longValue());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dump(Socket socket) {

        try {

            LOG.info("dump start...");

            /**
             * Receive alignment results
             */
            List<byte[]> rs = PSIUtils.receive2DBytes2(socket);
            List<JObject> fruit = new ArrayList<>();
            for (int i = 0; i < rs.size(); i++) {
                fruit.add(JObject.create(new String(rs.get(i))));
                fusionCount.increment();
            }

            LOG.info("fusionCount: " + fusionCount.longValue());

            //Put in storage
            dump(fruit);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
