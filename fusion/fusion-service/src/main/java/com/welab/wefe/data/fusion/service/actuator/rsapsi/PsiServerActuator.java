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
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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


    public PsiServerActuator fillBloomFilters(BloomFilters bloomFilters) {
        this.bf = bloomFilters;
        this.status = PSIActuatorStatus.running;
        return this;
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

    private void align(Socket socket) {

        LOG.info("align start...");

        long start = System.currentTimeMillis();

        try {

            byte[][] query = PSIUtils.receive2DBytes(socket);
            DataInputStream d_in = new DataInputStream(socket.getInputStream());
            int index = (int) PSIUtils.receiveInteger(d_in);

            LOG.info("server wait spend :  {} ms ", (System.currentTimeMillis() - start));

            long start1 = System.currentTimeMillis();

            //Encrypted again
            byte[][] result = CryptoUtils.sign(N, d, query);

            LOG.info("server a.mod(N) spend :  {} ms size: {}", (System.currentTimeMillis() - start1), result.length);

            /**
             * Return the query result
             */
            DataOutputStream d_out = new DataOutputStream(socket.getOutputStream());
            PSIUtils.send2DBytes(socket, result);
            PSIUtils.sendInteger(d_out, index);

            /**
             * Receive alignment results
             */
            List<byte[]> rs = PSIUtils.receive2DBytes2(socket);
            List<JObject> fruit = new ArrayList<>();
            for (int i = 0; i < rs.size(); i++) {
                fruit.add(JObject.create(new String(rs.get(i))));
                fusionCount.increment();
            }

            processedCount.add(result.length);
            LOG.info("processedCount: " + processedCount.longValue());
            LOG.info("fusionCount: " + fusionCount.longValue());

            //Put in storage
            dump(fruit);

            //Clean current batch
//            clear();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void clear() {
        LOG.info("cache clear");
        //removeAll
        fruit.clear();
    }

    private void end() {
        //Modify the state of
        this.status = PSIActuatorStatus.success;

        LOG.info("align end...");
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
        } else if (ActionType.end.name().equals(action)) {
            end();
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