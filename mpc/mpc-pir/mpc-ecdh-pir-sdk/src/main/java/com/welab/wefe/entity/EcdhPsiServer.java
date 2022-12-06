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

public class EcdhPsiServer {

    private static final Logger LOG = LoggerFactory.getLogger(EcdhPsiServer.class);

    private BigInteger serverPrivateD;
    private int threads = 4;
    private int threadTimeoutSeconds = 60 * 30;

    public EcdhPsiServer() {
        this.serverPrivateD = generaterPrivateKey();
    }

    /**
     * step 1 对自己的数据集进行加密
     */
    public Set<String> encryptDataset(Set<String> inputSet) {
        LOG.info("server begin encryptDataset");
        // 初始化椭圆曲线
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
        EllipticCurve ellipticCurve = new EllipticCurve(ecSpec);
        Set<String> encryptedSet = ConcurrentHashMap.newKeySet();
        List<Set<String>> partitionList = PartitionUtil.partitionSet(inputSet, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());

        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                for (String stringValue : partition) {
                    BigInteger bigIntegerValue = ConverterUtil.convertStringToBigInteger(stringValue);
                    ECPoint encryptedValue = EllipticCurve.multiply(ellipticCurve.mapMessage(bigIntegerValue),
                            this.serverPrivateD);
                    encryptedSet.add(ConverterUtil.convertECPointToString(encryptedValue));
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
        LOG.info("server end encryptDataset");
        return encryptedSet;
    }

    /**
     * step 2 对输入（客户端）的数据进行加密操作
     */
    public Map<Long, String> encryptDatasetMap(Map<Long, String> inputMap) {
        LOG.info("server begin encryptDatasetMap");
        // 初始化椭圆曲线
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
        EllipticCurve ellipticCurve = new EllipticCurve(ecSpec);
        ECCurve ecCurve = ellipticCurve.getEcCurve();

        Map<Long, String> encryptedMap = new ConcurrentHashMap<>();

        List<Map<Long, String>> partitionList = PartitionUtil.partitionMap(inputMap, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());

        for (Map<Long, String> partition : partitionList) {
            executorService.submit(() -> {
                for (Map.Entry<Long, String> entry : partition.entrySet()) {
                    // 将值转为椭圆曲线上的点
                    ECPoint ecPointValue = ConverterUtil.convertStringToECPoint(ecCurve, entry.getValue());
                    // 使用服务端私钥进行计算，得到加密后的值
                    ECPoint encryptedValue = EllipticCurve.multiply(ecPointValue, serverPrivateD);
                    encryptedMap.put(entry.getKey(), ConverterUtil.convertECPointToString(encryptedValue));
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
        LOG.info("server end encryptDatasetMap");
        return encryptedMap;
    }

    private BigInteger generaterPrivateKey() {
        LOG.info("server begin generaterPrivateKey");
        ECParameterSpec ecSpec;
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator keyGenerator;
        try {
            keyGenerator = KeyPairGenerator.getInstance("EC", "BC");
            ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
            keyGenerator.initialize(ecSpec, new SecureRandom());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("prime256v1 key generator not available");
        }
        KeyPair pair = keyGenerator.genKeyPair();
        LOG.info("server end generaterPrivateKey");
        return ((ECPrivateKey) pair.getPrivate()).getD();
    }

}
