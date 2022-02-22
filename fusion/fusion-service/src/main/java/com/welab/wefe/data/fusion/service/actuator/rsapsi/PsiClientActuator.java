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

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.enums.ActionType;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import com.welab.wefe.data.fusion.service.service.FieldInfoService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import com.welab.wefe.data.fusion.service.utils.SocketUtils;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import com.welab.wefe.data.fusion.service.utils.primarykey.PrimaryKeyUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author hunter.zhao
 */
public class PsiClientActuator extends AbstractPsiActuator {
    BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<Runnable>(5);
    RejectedExecutionHandler rejectedExecutionHandler =
            new ThreadPoolExecutor.CallerRunsPolicy();
    ExecutorService threadPool = new ThreadPoolExecutor(5, 10, 0L, TimeUnit.MILLISECONDS,
            workingQueue, rejectedExecutionHandler);

    ExecutorService parseThreadPool = new ThreadPoolExecutor(5, 10, 0L, TimeUnit.MILLISECONDS,
            workingQueue, rejectedExecutionHandler);

    private BigInteger e;
    private BigInteger N;
    private String dataSetId;
    private Boolean isTrace;
    private String traceColumn;


    private BlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<Socket>();

    // -------------↓↓↓↓The actuator caches data information during execution↓↓↓↓-------------//
    private Map<Integer, List<BigInteger>> r = new HashMap<>();
    private Map<Integer, List<BigInteger>> rInv = new HashMap<>();
    private Map<Integer, List<String>> data = new HashMap<>();
    private Map<Integer, List<JObject>> cacheMap = new HashMap<>();
    // -------------↑↑↑↑The actuator caches data information during execution↑↑↑↑-------------//

    private ThreadLocal<Integer> threadId = new ThreadLocal<>();

    /**
     * Fragment size, default 10000
     */
    private int shard_size = 2000;
    private int current_index = 0;

    public List<String> columnList;
    public List<FieldInfo> fieldInfoList;

    public PsiClientActuator(String businessId, Integer dataCount, String ip, int port, String dataSetId, Boolean isTrace, String traceColumn) {
        super(businessId, dataCount);
        this.ip = ip;
        this.port = port;
        this.dataSetId = dataSetId;
        this.isTrace = isTrace;
        this.traceColumn = traceColumn;
    }

    /**
     * Paging data fetching
     *
     * @return
     * @throws StatusCodeWithException
     */
    public synchronized boolean cursor() throws StatusCodeWithException {
        long start = System.currentTimeMillis();

        DataSetService service = Launcher.CONTEXT.getBean(DataSetService.class);
        List<JObject> curList = service.paging(columnList, dataSetId, current_index, shard_size);
        current_index++;

        LOG.info("cursor {} spend: {}", current_index, System.currentTimeMillis() - start);

        if (curList.isEmpty()) {
            return false;
        }

        cacheMap.put(current_index, curList);
        threadId.set(current_index);
        return true;
    }


    /**
     * Download the Server Square Bloom filter
     */
    private void downloadBloomFilter() {
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
            e = PSIUtils.bytesToBigInteger(pk[0], 0, pk[0].length);
            N = PSIUtils.bytesToBigInteger(pk[1], 0, pk[1].length);
            DataInputStream d_in = new DataInputStream(socket.getInputStream());
            int DB_size = (int) PSIUtils.receiveInteger(d_in);
            int bitSetSize = (int) PSIUtils.receiveInteger(d_in);
            byte[] b = PSIUtils.receiveBytes(socket);
            BitSet bs = BitSet.valueOf(b);

            bf = new BloomFilters(bitSetSize, DB_size, DB_size, bs);
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
    }

    /**
     * Begin to align
     */
    private void align() throws StatusCodeWithException {

        LOG.info("client aligning..., count: {} availableProcessors: {}", dataCount, Runtime.getRuntime().availableProcessors());

        long startTime = System.currentTimeMillis();

        //The number of batches is calculated by slice size and data volume
        int count = dataCount % shard_size == 0 ? dataCount / shard_size
                : dataCount / shard_size + 1;

        /**
         * Data encryption
         */
        CountDownLatch latch = new CountDownLatch(count);
        LOG.info("Start data encryption...");


        LOG.info("Server@" + ip + ":" + port + " connecting!");
        Socket socket = SocketUtils
                .create(ip, port)
                .setRetryCount(3)
                .builder();

        for (int i = 0; i < count; i++) {
            threadPool.execute(() -> {
                try {
                    fusion(socket);

                    //TODO 临时代码
//                    Socket socket = socketQueue.take();
//                    if (socket != null) {
//                        receiveAndParseResult(socket);
//                    }
                } catch (StatusCodeWithException e) {
                    e.printStackTrace();
                    LOG.error("{} StatusCodeWithException : {}", getClass().getSimpleName(), e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        /**
         * Alignment matching
         */
//        int socketQueueSize = count;
//        CountDownLatch socketLatch = new CountDownLatch(count);
//
//        while (socketQueueSize > 0) {
//            try {
//                Socket socket = socketQueue.take();
//                if (socket != null) {
//                    parseThreadPool.execute(() -> {
//                        receiveAndParseResult(socket);
//                        socketLatch.countDown();
//                    });
//                    socketQueueSize--;
//                }
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//            }
//        }
//
        try {
            latch.await();
//            socketLatch.await();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            LOG.error("{} InterruptedException : {}", getClass().getSimpleName(), e1.getMessage());
        }

        LOG.info("-----------------Time used: {} ", (System.currentTimeMillis() - startTime));

        this.status = PSIActuatorStatus.success;

        //Notifies the server that no further action is required
        Socket closeSocket = SocketUtils
                .create(ip, port)
                .setRetryCount(3)
                .builder();
        PSIUtils.sendString(closeSocket, ActionType.end.name());
        SocketUtils.close(closeSocket);
    }

    /**
     * Data fusion
     *
     * @throws StatusCodeWithException
     */
    private void fusion(Socket socket) throws StatusCodeWithException {
//        Socket socket = null;
//        try {
//            LOG.info("Server@" + ip + ":" + port + " connecting!");
//            socket = SocketUtils
//                    .create(ip, port)
//                    .setRetryCount(3)
//                    .builder();

            PSIUtils.sendString(socket, ActionType.align.name());

            cursor();

            Integer index = threadId.get();

            LOG.info("fusion() current_index ： {}", index);

            //Initiating a query request
            query(socket);

            //Joins the queue to be parsed
            //socketQueue.put(socket);
            receiveAndParseResult(socket);
//        } catch (InterruptedException e1) {
//            e1.printStackTrace();
//        }

    }

    /**
     * After data is encrypted, query verification is initiated
     */
    private void query(Socket socket) {
        long start = System.currentTimeMillis();

        Integer index = threadId.get();
        List<JObject> cur = cacheMap.get(index);
        List<String> d = new ArrayList<>();
        List<BigInteger> r = new ArrayList<>();
        List<BigInteger> rInv = new ArrayList<>();

        byte[][] bs = new byte[cur.size()][];

        for (int i = 0; i < cur.size(); i++) {
            //Handle the primary key according to the keyPrimary method
            String key = PrimaryKeyUtils.create(cur.get(i), fieldInfoList);
            d.add(key);

            BigInteger h = PSIUtils.stringToBigInteger(key);
            BigInteger blindFactor = generateBlindingFactor();
            r.add(blindFactor.modPow(e, N));
            rInv.add(blindFactor.modInverse(N));
            BigInteger x = h.multiply(r.get(i)).mod(N);
            bs[i] = PSIUtils.bigIntegerToBytes(x, false);
        }

        data.put(index, d);
        this.r.put(index, r);
        this.rInv.put(index, rInv);

        LOG.info("query() current_index ： {} client r.mod(N) spend : {} ms", index, System.currentTimeMillis() - start);

        LOG.info("client send fusion data...");

        sendData(socket, bs, index);
    }

    void clear(Integer index) {
        LOG.info("cache clear");
        cacheMap.remove(index);
        data.remove(index);
        r.remove(index);
        rInv.remove(index);
        threadId.remove();
    }

    /**
     * Receives encrypted data, parses and matches
     */
    private void receiveAndParseResult(Socket socket) {

        LOG.info("client start receive data...");

        long start = System.currentTimeMillis();

        try {
            byte[][] ret = PSIUtils.receive2DBytes(socket);
            DataInputStream d_in = new DataInputStream(socket.getInputStream());
            Integer index = (int) PSIUtils.receiveInteger(d_in);

            LOG.info("receiveAndParseResult() current_index ： {} ", index);

            List<JObject> cur = cacheMap.get(index);
            List<byte[]> rs = new ArrayList<>();
            List<JObject> fruit = new ArrayList<>();
            for (int i = 0; i < ret.length; i++) {

                BigInteger y = PSIUtils.bytesToBigInteger(ret[i], 0, ret[i].length);
                BigInteger z = y.multiply(rInv.get(index).get(i)).mod(N);

                if (bf.contains(z)) {
                    rs.add(cur.get(i).toString().getBytes());
                    fruit.add(cur.get(i));
                    fusionCount.increment();
                }

                processedCount.increment();
            }

            LOG.info("client y.mod(N) spend : " + (System.currentTimeMillis() - start) + " ms");

            /**
             * Send alignment data to the server
             */
            PSIUtils.send2DBytes(socket, rs);

            LOG.info("fusionCount: " + fusionCount.longValue());
            LOG.info("processedCount: " + processedCount.longValue());

            dump(fruit);

            //Clear the cache
            clear(index);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Send data with an index
     *
     * @param bs
     * @param index
     */
    private void sendData(Socket socket, byte[][] bs, int index) {
        try {
            DataOutputStream d_out = new DataOutputStream(socket.getOutputStream());
            PSIUtils.send2DBytes(socket, bs);
            PSIUtils.sendInteger(d_out, index);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private BigInteger generateBlindingFactor() {
        BigInteger ZERO = BigInteger.valueOf(0);
        BigInteger ONE = BigInteger.valueOf(1);
        int length = N.bitLength() - 1;
        BigInteger gcd;
        BigInteger blindFactor = new BigInteger(length, new SecureRandom());
        do {
            gcd = blindFactor.gcd(N);
        }
        while (blindFactor.equals(ZERO) || blindFactor.equals(ONE) || !gcd.equals(ONE));

        return blindFactor;
    }

    private void execute(ActionType action) throws StatusCodeWithException {

        switch (action) {
            case download:
                /**
                 * download
                 */
                downloadBloomFilter();
                break;
            case align:
                align();
                break;
            default:
                throw new RuntimeException("Unexpected enumeration：" + action);
        }

        //Update time
        lastLogTime = System.currentTimeMillis();
    }


    @Override
    public void close() throws Exception {
//        if (socket != null) {
//            socket.close();
//        }
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
    public void handle() throws StatusCodeWithException {
        status = PSIActuatorStatus.running;

        //Download bloom filter
        execute(ActionType.download);

        //align
        execute(ActionType.align);
    }
}
