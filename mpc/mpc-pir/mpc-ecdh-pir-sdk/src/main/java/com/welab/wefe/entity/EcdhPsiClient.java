/*
 * Copyright 2022 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.entity;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.util.ConverterUtil;
import com.welab.wefe.util.PartitionUtil;

public class EcdhPsiClient {

    private static final Logger LOG = LoggerFactory.getLogger(EcdhPsiClient.class);

    private BigInteger clientPrivateD; // 客户端私钥
    private ECCurve ecCurve; // 椭圆曲线
    private EllipticCurve ellipticCurve; // 椭圆曲线实现类
    private Map<Long, BigInteger> clientOriginalDatasetMap; // 保存已经处理过的数据，key为自增序列ID
    private Map<Long, ECPoint> clientDoubleEncryptedDatasetMap; // 保存经过服务端二次加密后客户端的数据
    private Set<ECPoint> serverDoubleEncryptedDataset; // 保存经过自己二次加密后的服务端的数据

    private AtomicLong idAtomicCounter; // id 计数器
    private int threads = 4;
    protected int threadTimeoutSeconds = 60 * 30;

    public EcdhPsiClient() {
        this.serverDoubleEncryptedDataset = ConcurrentHashMap.newKeySet();
        this.clientOriginalDatasetMap = new ConcurrentHashMap<>();
        this.clientDoubleEncryptedDatasetMap = new ConcurrentHashMap<>();

        this.idAtomicCounter = new AtomicLong(0);
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
        this.ellipticCurve = new EllipticCurve(ecSpec);
        this.ecCurve = ecSpec.getCurve();
        this.clientPrivateD = generaterPrivateKey(ecSpec);
    }

    /**
     * step 5 求交
     */
    public Set<String> psi() {
        LOG.info("client begin psi");
        Set<String> psi = ConcurrentHashMap.newKeySet();
        // 对客户端的数据进行分片
        List<Map<Long, ECPoint>> reversedMapPartition = PartitionUtil.partitionMap(this.clientDoubleEncryptedDatasetMap,
                this.threads);
        ExecutorService executorService = Executors.newFixedThreadPool(reversedMapPartition.size());
        for (Map<Long, ECPoint> partition : reversedMapPartition) {
            executorService.submit(() -> {
                for (Map.Entry<Long, ECPoint> entry : partition.entrySet()) {
                    // 如果服务端的数据中存在客户端的数据
                    if (this.serverDoubleEncryptedDataset.contains(entry.getValue()))
                        psi.add(ConverterUtil
                                .convertBigIntegerToString(clientOriginalDatasetMap.get(entry.getKey())));
                }
            });
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(threadTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            executorService.shutdown();
        }
        LOG.info("client end psi");
        return psi;
    }

    /**
     * step 4 将经过服务端二次加密后的客户端数据集转换成椭圆曲线上的点
     */
    public void convertDoubleEncryptedClientDataset2ECPoint(Map<Long, String> doubleEncryptedClientDatasetMap) {
        LOG.info("client begin convertDoubleEncryptedClientDataset2ECPoint");
        for (Map.Entry<Long, String> entry : doubleEncryptedClientDatasetMap.entrySet()) {
            this.clientDoubleEncryptedDatasetMap.put(entry.getKey(),
                    ConverterUtil.convertStringToECPoint(this.ecCurve, entry.getValue()));
        }
        LOG.info("client end convertDoubleEncryptedClientDataset2ECPoint");
    }

    /**
     * step 3 客户端使用私钥多线程加密服务端的数据集
     */
    public void encryptServerDataset(Set<String> serverEncryptedDataset) {
        LOG.info("client begin encryptServerDataset");
        List<Set<String>> partitionList = PartitionUtil.partitionSet(serverEncryptedDataset, this.threads);

        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());

        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                for (String serverEncryptedEntry : partition) {
                    ECPoint ecPointValue = ConverterUtil.convertStringToECPoint(this.ecCurve,
                            serverEncryptedEntry);
                    ECPoint encryptedValue = EllipticCurve.multiply(ecPointValue, this.clientPrivateD);
                    this.serverDoubleEncryptedDataset.add(encryptedValue);
                }
            });
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(threadTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            executorService.shutdown();
        }
        LOG.info("client end encryptServerDataset");
    }

    /**
     * 
     * step 2 客户端使用私钥多线程加密自己的数据集
     */
    public Map<Long, String> encryptClientOriginalDataset(Set<String> originalDataset) {
        LOG.info("client begin encryptOriginalDataset");
        List<Set<String>> clientDatasetPartitions = PartitionUtil.partitionSet(originalDataset, threads);
        Map<Long, String> clientEncryptedDatasetMap = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(clientDatasetPartitions.size());

        for (Set<String> partition : clientDatasetPartitions) {
            executorService.submit(() -> {
                for (String value : partition) {
                    BigInteger bigIntegerValue = ConverterUtil.convertString2BigInteger(value);
                    ECPoint encryptedValue = EllipticCurve.multiply(this.ellipticCurve.mapMessage(bigIntegerValue),
                            clientPrivateD);
                    Long key = idAtomicCounter.incrementAndGet();
                    clientOriginalDatasetMap.put(key, bigIntegerValue);
                    clientEncryptedDatasetMap.put(key, ConverterUtil.convertECPointToString(encryptedValue));
                }
            });
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(threadTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            executorService.shutdown();
        }
        LOG.info("client end encryptOriginalDataset");
        return clientEncryptedDatasetMap;
    }

    /**
     * step 1 生成一个私钥
     */
    private BigInteger generaterPrivateKey(ECParameterSpec ecSpec) {
        LOG.info("client begin generaterPrivateKey");
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGenerator;
        try {
            keyGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyGenerator.initialize(ecSpec, new SecureRandom());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("EC key generator not available");
        }
        KeyPair pair = keyGenerator.genKeyPair();
        LOG.info("client end generaterPrivateKey");
        return ((ECPrivateKey) pair.getPrivate()).getD();
    }

    /**
     * 生成一个私钥
     */
    private BigInteger generaterPrivateKey1(ECParameterSpec ecSpec) {
        BigInteger n = ecSpec.getN();
        // 随机状态
        SecureRandom secureRandom = new SecureRandom();
        BigInteger k = new BigInteger(n.bitLength(), secureRandom).mod(n);
        return k;
    }

}
