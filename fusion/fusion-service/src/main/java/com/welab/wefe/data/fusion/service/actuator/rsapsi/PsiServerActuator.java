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

package com.welab.wefe.data.fusion.service.actuator.rsapsi;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.data.fusion.service.enums.ActionType;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import com.welab.wefe.data.fusion.service.utils.FusionUtils;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class PsiServerActuator extends AbstractPsiActuator {
    private ServerSocket serverSocket;

    private BigInteger N;
    private BigInteger d;
    private BigInteger e;


    public PsiServerActuator(String businessId, Integer dataCount, String ip, int port, BigInteger N, BigInteger e, BigInteger d) {
        super(businessId, dataCount);
        this.N = N;
        this.e = e;
        this.d = d;
        this.ip = ip;
        this.port = port;
    }


    private Map<Integer, byte[][]> cacheMap = new HashMap<>();
    // -------------↑↑↑↑The actuator caches data information during execution↑↑↑↑-------------//

    private ThreadLocal<Integer> threadId = new ThreadLocal<>();

    public PsiServerActuator fillBloomFilters(BloomFilters bloomFilters) {
        this.bf = bloomFilters;
        this.status = PSIActuatorStatus.running;
        return this;
    }

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(9090);
            System.out.println("Server@" + InetAddress.getLocalHost() + " start!");

            Thread server = new Thread(() -> {
                try {
                    while (true) {
                        // listen PORT;
                        Socket socket = serverSocket.accept();

                        List<String> dataBody = PSIUtils.receiveStringList(socket);
                        if (CollectionUtils.isEmpty(dataBody)) {
                            return;
                        }

                        String action = FusionUtils.extractAction(dataBody);
                    }
                } catch (Exception e) {
                } finally {
                }
            });
            server.start();
        } catch (IOException e) {

        }


    }



    public void start() throws StatusCodeWithException {
        if (bf == null) {
            throw new StatusCodeWithException("数据未初始化无法启动", StatusCode.UNSUPPORTED_HANDLE);
        }

        LOG.info("PsiServerActuator start...");

        try {
            serverSocket = new ServerSocket(port);
            LOG.info("Server@" + InetAddress.getLocalHost() + " start!");
        } catch (IOException e) {
            LOG.error(e.getClass().getSimpleName() + "server socket start error:" + e.getMessage());
        }

        Thread server = new Thread(() -> listen());
        server.start();
    }

    private void listen() {
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


    private void sendBloomFilter(Socket socket) {

        LOG.info("server send bloom_filter data...");
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

    private void align(Socket socket, List<String> dataBody) {

        LOG.info("align start...");

        long start = System.currentTimeMillis();

        try {

//            dataBody.remove(0);
//            byte[][] query = PSIUtils.receive2DBytes(socket);
            Integer index = FusionUtils.extractIndex(dataBody);
            byte[][] queryBody = FusionUtils.extractData(dataBody);
            LOG.info("server wait spend :  {} ms  current_index: {}", (System.currentTimeMillis() - start), index);

            long start1 = System.currentTimeMillis();

            //Encrypted again
            byte[][] result = CryptoUtils.sign(N, d, queryBody);

            LOG.info("server a.mod(N) spend :  {} ms size: {}  current_index: {}", (System.currentTimeMillis() - start1), result.length, index);

            /**
             * Return the query result
             */
            FusionUtils.sendByteAndIndex(socket, result, index);

            processedCount.add(result.length);

            LOG.info("processedCount: " + processedCount.longValue());

        } catch (Exception e) {
            LOG.error("数据融合错误，ERROR：", e);
        }
    }

    private void receiveResult(List<String> dataBody) {
        LOG.info("dataBody size: " + processedCount.longValue());
        /**
         * Receive alignment results
         */
        byte[][] rs = FusionUtils.extractData(dataBody);
        List<JObject> fruit = new ArrayList<>();
        for (int i = 0; i < rs.length; i++) {
            fruit.add(JObject.create(new String(rs[i])));
            fusionCount.increment();
        }

        LOG.info("fusionCount: " + fusionCount.longValue());

        //Put in storage
        dump(fruit);
    }

    private void end(List<String> body) {
        //Modify the state of
        this.status = PSIActuatorStatus.valueOf(body.get(0));

        LOG.info("align end...,status is {}", this.status);
    }

    private void execute(Socket socket) {

        List<String> dataBody = PSIUtils.receiveStringList(socket);
        if (CollectionUtils.isEmpty(dataBody)) {
            return;
        }

        String action = FusionUtils.extractAction(dataBody);
        LOG.info("执行execute：{}", action);
        if (ActionType.download.name().equals(action)) {
            /**
             * download
             */
            sendBloomFilter(socket);
        } else if (ActionType.align.name().equals(action)) {
            align(socket, dataBody);
        } else if (ActionType.fusion.name().equals(action)) {
            receiveResult(dataBody);
        } else if (ActionType.end.name().equals(action)) {
            end(dataBody);
        }

        //Update last time
        lastLogTime = System.currentTimeMillis();
    }

    @Override
    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            LOG.warn(e.getClass().getSimpleName() + " close error:" + e.getMessage());
        }
    }


    @Override
    public void init() throws StatusCodeWithException {
    }

    @Override
    public void handle() throws StatusCodeWithException {
        this.start();
    }
}