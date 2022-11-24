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

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Base64Util;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.dto.entity.PartnerOutputModel;
import com.welab.wefe.data.fusion.service.enums.ActionType;
import com.welab.wefe.data.fusion.service.enums.CallbackType;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import com.welab.wefe.data.fusion.service.service.FieldInfoService;
import com.welab.wefe.data.fusion.service.service.ThirdPartyService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import com.welab.wefe.data.fusion.service.utils.FusionUtils;
import com.welab.wefe.data.fusion.service.utils.SocketUtils;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import com.welab.wefe.data.fusion.service.utils.primarykey.PrimaryKeyUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;

/**
 * @author hunter.zhao
 */
public class PsiClientActuator extends AbstractPsiActuator {
    ExecutorService threadPool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            100L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    private BigInteger e;
    private BigInteger N;
    private String dataSetId;
    private Boolean isTrace;
    private String traceColumn;
    private PartnerOutputModel partnerModel;

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

    private final DataSetService dataSetService = Launcher.CONTEXT.getBean(DataSetService.class);
    private final ThirdPartyService thirdPartyService = Launcher.CONTEXT.getBean(ThirdPartyService.class);
    
    public PsiClientActuator(String businessId, Integer dataCount, String ip, int port, String dataSetId,
            Boolean isTrace, String traceColumn, PartnerOutputModel partnerModel) {
        super(businessId, dataCount);
        this.ip = ip;
        this.port = port;
        this.dataSetId = dataSetId;
        this.isTrace = isTrace;
        this.traceColumn = traceColumn;
        this.partnerModel = partnerModel;
    }

    /**
     * Paging data fetching
     *
     * @return
     * @throws StatusCodeWithException
     */
    public synchronized boolean cursor() throws StatusCodeWithException {
        long start = System.currentTimeMillis();

        List<JObject> curList = dataSetService.paging(columnList, dataSetId, current_index, shard_size);
        current_index++;

        LOG.info("fusion task log , cursor {} size: {} spend: {} ", current_index, curList.size(), System.currentTimeMillis() - start);

        if (CollectionUtils.isEmpty(curList)) {
            return false;
        }

        cacheMap.put(current_index, curList);
        threadId.set(current_index);
        return true;
    }


    public static void main(String[] args) {
        Socket socket = SocketUtils
                .create("127.0.0.1", 9090)
                .setRetryCount(3)
                .setRetryDelay(1000)
                .builder();


        List list = Lists.newArrayList();
        list.add(ActionType.download.name());
        PSIUtils.sendStringList(socket, list);
    }

    /**
     * Download the Server Square Bloom filter
     * @throws StatusCodeWithException 
     */
    private void downloadBloomFilter() throws StatusCodeWithException {
        Socket socket = null;
        try {
            LOG.info("fusion task log , Server@" + ip + ":" + port + " connecting!");
            int count = 0;
            while (socket == null && count < 10) {
                socket = SocketUtils.create(ip, port).setRetryCount(3).setRetryDelay(1000).builder();
                count++;
            }
            if(socket == null) {
                LOG.error("fusion task log , socket connect error");
                this.status = PSIActuatorStatus.exception;
                throw new StatusCodeWithException(StatusCode.REMOTE_SERVICE_ERROR, "connect " + ip + ":" + port + "error");
            }
            LOG.info("fusion task log , socket: {} ", socket);

            // 告知server端，把bloomfilter传过来
            List<String> body = new ArrayList();
            body.add(ActionType.download.name());
            PSIUtils.sendStringList(socket, body);

            LOG.info("fusion task log , client download bloom_filter data...");

            byte[][] pk = PSIUtils.receive2DBytes(socket);
            e = PSIUtils.bytesToBigInteger(pk[0], 0, pk[0].length);
            N = PSIUtils.bytesToBigInteger(pk[1], 0, pk[1].length);
            DataInputStream d_in = new DataInputStream(socket.getInputStream());
            int DB_size = (int) PSIUtils.receiveInteger(d_in);
            int bitSetSize = (int) PSIUtils.receiveInteger(d_in);
            byte[] b = PSIUtils.receiveBytes(socket);
            BitSet bs = BitSet.valueOf(b);
            LOG.info("fusion task log , download bf success : e = " + e);
            LOG.info("fusion task log , download bf success : N = " + N);
            LOG.info("fusion task log , download bf success : DB_size = " + DB_size);
            LOG.info("fusion task log , download bf success : bitSetSize = " + bitSetSize);
            bf = new BloomFilters(bitSetSize, DB_size, DB_size, bs);
            LOG.info("fusion task log , download bf success : " + JSONObject.toJSONString(bf));
        } catch (IOException e1) {
            LOG.error("fusion task log , download bf error : ", e1);
            //Interrupt tasks
            this.status = PSIActuatorStatus.exception;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                LOG.error("fusion task log , download bf error : ", e1);
            }
        }
    }

    /**
     * Begin to align
     */
    private void align() throws StatusCodeWithException {

        LOG.info("fusion task log , client aligning..., count: {} availableProcessors: {}", dataCount, Runtime.getRuntime().availableProcessors());

        long startTime = System.currentTimeMillis();

        //The number of batches is calculated by slice size and data volume
        int count = dataCount % shard_size == 0 ? dataCount / shard_size
                : dataCount / shard_size + 1;

        /**
         * Data encryption
         */
        CountDownLatch latch = new CountDownLatch(count);
        LOG.info("fusion task log , Start data encryption...");


        for (int i = 0; i < count; i++) {
            threadPool.execute(() -> {
                try {
                    fusion();
                } catch (StatusCodeWithException e) {
                    e.printStackTrace();
                    LOG.error("fusion task log , StatusCodeWithException :", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            LOG.error("fusion task log ,  InterruptedException :",e1);
        }

        LOG.info("fusion task log , -----------------Time used: {} ", (System.currentTimeMillis() - startTime));

        this.status = PSIActuatorStatus.success;
    }

    /**
     * Data fusion
     *
     * @throws StatusCodeWithException
     */
    private void fusion() throws StatusCodeWithException {

        cursor();

        //Initiating a query request
        LOG.info("fusion task log , Server@" + ip + ":" + port + " connecting!");
        Socket socket = SocketUtils
                .create(ip, port)
                .setRetryCount(3)
                .builder();

        query(socket);

        receiveAndParseResult(socket);
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

        List<String> body = new ArrayList();
        body.add(ActionType.align.name());
        body.add(index.toString());

//        byte[][] bs = new byte[cur.size()][];

        for (int i = 0; i < cur.size(); i++) {
            //Handle the primary key according to the keyPrimary method
            String key = PrimaryKeyUtils.create(cur.get(i), fieldInfoList);
            d.add(key);

            BigInteger h = PSIUtils.stringToBigInteger(key);
            BigInteger blindFactor = generateBlindingFactor();
            r.add(blindFactor.modPow(e, N));
            rInv.add(blindFactor.modInverse(N));
            BigInteger x = h.multiply(r.get(i)).mod(N);
//            bs[i] = PSIUtils.bigIntegerToBytes(x, false);
            body.add(Base64Util.encode(PSIUtils.bigIntegerToBytes(x, false)));
        }

        data.put(index, d);
        this.r.put(index, r);
        this.rInv.put(index, rInv);

        LOG.info("fusion task log , query() current_index ： {} client r.mod(N) spend : {} ms", index, System.currentTimeMillis() - start);

        LOG.info("fusion task log , client send fusion data...");

        PSIUtils.sendStringList(socket, body);

//        FusionUtils.sendByteAndIndex(socket, bs, index);
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

        LOG.info("fusion task log , client start receive data...");

        long start = System.currentTimeMillis();

        try {
            byte[][] repBody = PSIUtils.receive2DBytes(socket);
            Integer index = FusionUtils.extractIndex(repBody);
            byte[][] ret = FusionUtils.extractData(repBody);
            LOG.info("fusion task log , receiveAndParseResult() current_index ： {} ", index);

            List<JObject> cur = cacheMap.get(index);
            List<String> rs = new ArrayList();
            rs.add(ActionType.fusion.name());

            List<JObject> fruit = new ArrayList<>();
            for (int i = 0; i < ret.length; i++) {

                BigInteger y = PSIUtils.bytesToBigInteger(ret[i], 0, ret[i].length);
                BigInteger z = y.multiply(rInv.get(index).get(i)).mod(N);
                LOG.info("fusion task log , z： {} ", index);
                if (bf.contains(z)) {
                    rs.add(Base64Util.encode(cur.get(i).toString().getBytes()));
                    fruit.add(cur.get(i));
                    fusionCount.increment();
                }

                processedCount.increment();
            }

            LOG.info("fusion task log , client y.mod(N) spend : " + (System.currentTimeMillis() - start) + " ms");

            /**
             * Send alignment data to the server
             */
            LOG.info("fusion task log , Server@" + ip + ":" + port + " connecting!");
            Socket socketResult = SocketUtils
                    .create(ip, port)
                    .setRetryCount(3)
                    .builder();
            PSIUtils.sendStringList(socketResult, rs);

            LOG.info("fusionCount: " + fusionCount.longValue());
            LOG.info("processedCount: " + processedCount.longValue());

            dump(fruit);

            //Clear the cache
            clear(index);
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
        if(!status.name().equalsIgnoreCase(PSIActuatorStatus.success.name())) {
            closeByHttp(CallbackType.stop);
        }
        // Notifies the server that no further action is required
        Socket closeSocket = SocketUtils.create(ip, port).setRetryCount(3).builder();
        List<String> stringList = new ArrayList<>();
        stringList.add(ActionType.end.name());
        stringList.add(status.name());
        PSIUtils.sendStringList(closeSocket, stringList);
        SocketUtils.close(closeSocket);
    }

    public void closeByHttp(CallbackType type) throws StatusCodeWithException {
        // The callback
        if (this.partnerModel != null) {
            JSONObject response = thirdPartyService.callback(partnerModel.getBaseUrl(), businessId, type, -1);
            LOG.info("fusion task log ,closeByHttp callback, url = " + partnerModel.getBaseUrl() + ", response = "
                    + JSONObject.toJSONString(response));
        }

    }

    @Override
    public void init() throws StatusCodeWithException {
        FieldInfoService service = Launcher.CONTEXT.getBean(FieldInfoService.class);

        columnList = service.columnList(businessId);


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
         * Calculate the fragment size based on the number of fields
         */
        shard_size = shard_size / fieldInfoList.size();


        int dataSetRowCount = dataSetService.count(dataSetId);
        if (dataSetRowCount != dataCount) {
            LOG.error("数据集 {} 数据量有误！！", dataSetId);
            status = PSIActuatorStatus.falsify;
        }

        LOG.info("数据量 {}", dataSetRowCount);
    }

    @Override
    public void handle() throws StatusCodeWithException {
        status = PSIActuatorStatus.running;

        //Download bloom filter
        execute(ActionType.download);

        //align
        execute(ActionType.align);
    }
    
    public PartnerOutputModel getPartnerModel() {
        return partnerModel;
    }

    public void setPartnerModel(PartnerOutputModel partnerModel) {
        this.partnerModel = partnerModel;
    }
}
