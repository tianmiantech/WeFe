/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.mpc.pir.server.service;

import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.AccountEncryptionType;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.commom.RandomPhoneNum;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;
import com.welab.wefe.mpc.pir.protocol.ot.hauck.HauckObliviousTransfer;
import com.welab.wefe.mpc.pir.protocol.se.SymmetricKey;
import com.welab.wefe.mpc.pir.protocol.se.aes.AESDecryptKey;
import com.welab.wefe.mpc.pir.request.*;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: eval
 * @Date: 2021-12-27
 **/
@RunWith(PowerMockRunner.class)
@PrepareForTest({UUID.class})
@PowerMockIgnore("javax.crypto.*")
public class HuackKeyServiceTest extends TestCase {
    private final static Logger logger = LoggerFactory.getLogger(HuackKeyServiceTest.class);

    public void testHandle() throws Exception {
        logger.debug("start pir");
        long start = System.currentTimeMillis();
        int phoneCount = 10;
        int target = new Random().nextInt(phoneCount);
        logger.debug("target:{}", target);
        // 查询手机号
        List<String> mobiles = RandomPhoneNum.getKeys(phoneCount, "", AccountEncryptionType.md5.name());

        // 查询手机号对应的数据库值
        Map<String, String> results = new HashMap<>(mobiles.size());
        mobiles.stream().forEach(mobile -> results.put(mobile, mobile));

        // step 1: send keys
        QueryKeysRequest keysRequest = new QueryKeysRequest();
        keysRequest.setIds((List) mobiles);
        keysRequest.setMethod(AccountEncryptionType.md5.name());

        HuackKeyService keyService = new HuackKeyService();
        QueryKeysResponse keysResponse = keyService.handle(keysRequest);

        String uuid = keysResponse.getUuid();
        CacheOperation<Map<String, String>> dataResults = CacheOperationFactory.getCacheOperation();
        dataResults.save(uuid, Constants.RESULT, results);

        // step 2: generate Random R
        int keySize = 1024;
        BigInteger x = DiffieHellmanUtil.generateRandomKey(keySize);
        HauckObliviousTransfer hauckObliviousTransfer = new HauckObliviousTransfer(keysResponse.getUuid());
        GroupElement paramS = hauckObliviousTransfer.getGroupElement(keysResponse.getS());
        CompletableFuture<GroupElement> calcRcf = CompletableFuture.supplyAsync(() -> calcR(hauckObliviousTransfer, target, x, paramS));
        CompletableFuture<GroupElement> calcXs = CompletableFuture.supplyAsync(() -> hauckObliviousTransfer.arithmetic.mul(x, paramS));
        CompletableFuture.allOf(calcRcf, calcXs).join();
        GroupElement r = calcRcf.get();

        QueryRandomLegalRequest randomLegalRequest = new QueryRandomLegalRequest();
        randomLegalRequest.setsLegal(true);
        randomLegalRequest.setAttemptCount(0);
        randomLegalRequest.setR(Conversion.groupElementToString(r));
        randomLegalRequest.setUuid(keysResponse.getUuid());

        HauckRandomLegalService randomLegalService = new HauckRandomLegalService();
        QueryRandomLegalResponse randomLegalResponse = randomLegalService.handle(randomLegalRequest);
        List<String> resResults = randomLegalResponse.getResults();
        if (resResults == null || resResults.isEmpty()) {
            QueryPIRResultsRequest pirResultsRequest = new QueryPIRResultsRequest();
            pirResultsRequest.setUuid(keysResponse.getUuid());
            HuackResultsService resultsService = new HuackResultsService();
            QueryPIRResultsResponse pirResultsResponse = resultsService.handle(pirResultsRequest);
            resResults = pirResultsResponse.getResults();
        }

        // step 3: decrypt result
        GroupElement xs = calcXs.get();
        byte[] key = calcKey(hauckObliviousTransfer, xs, paramS, r);

        String enResults = resResults.get(target);
        String[] realResult = enResults.split(",");
        byte[] enResult = Conversion.hexStringToBytes(realResult[0]);
        byte[] iv = Conversion.hexStringToBytes(realResult[1]);
        SymmetricKey aesKey = new AESDecryptKey(key, iv);
        byte[] result = aesKey.encrypt(enResult);
        String resultValue = new String(result, Charset.defaultCharset());
        Assert.assertEquals(mobiles.get(target), resultValue);
        logger.debug("end pir. cost:" + (System.currentTimeMillis() - start) + "ms");
    }

    private byte[] calcKey(HauckObliviousTransfer hauckObliviousTransfer, BigInteger x, GroupElement paramS, GroupElement r) {
        GroupElement xs = hauckObliviousTransfer.arithmetic.mul(x, paramS);
        hauckObliviousTransfer.initMac(paramS, r);
        return hauckObliviousTransfer.macTecElement(xs);
    }

    private byte[] calcKey(HauckObliviousTransfer hauckObliviousTransfer, GroupElement xs, GroupElement paramS, GroupElement r) {
        hauckObliviousTransfer.initMac(paramS, r);
        return hauckObliviousTransfer.macTecElement(xs);
    }

    private GroupElement calcR(HauckObliviousTransfer hauckObliviousTransfer, int target, BigInteger x, GroupElement s) {
        GroupElement t = hauckObliviousTransfer.hashTecElement(s);
        BigInteger c = BigInteger.valueOf(target);
        GroupElement ct = hauckObliviousTransfer.arithmetic.mul(c, t);
        GroupElement xg = hauckObliviousTransfer.arithmetic.mul(x, hauckObliviousTransfer.arithmetic.getGenerator());
        GroupElement r = hauckObliviousTransfer.arithmetic.add(ct, xg);
        return r;
    }
}