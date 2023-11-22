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

package com.welab.wefe.mpc.sa.server.service;

import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyRequest;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyResponse;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Matchers.anyInt;

/**
 * @Author: eval
 * @Date: 2021-12-24
 **/
@RunWith(PowerMockRunner.class)
@PrepareForTest({
        DiffieHellmanUtil.class
})
public class QueryDiffieHellmanKeyServiceTest extends TestCase {

    public void testHandle() throws Exception {
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
        DiffieHellmanKey diffieHellmanKey = new DiffieHellmanKey();
        diffieHellmanKey.setG(new BigInteger("2", 16));
        diffieHellmanKey.setP(new BigInteger("ffffffffffffffffc90fdaa22168c234c4c6628b80dc1cd129024e088a67cc74020bbea63b139b22514a08798e3404ddef9519b3cd3a431b302b0a6df25f14374fe1356d6d51c245e485b576625e7ec6f44c42e9a637ed6b0bff5cb6f406b7edee386bfb5a899fa5ae9f24117c4b1fe649286651ece65381ffffffffffffffff", 16));

        BigInteger key = new BigInteger(1024, new Random(99999));
        String value = diffieHellmanKey.getG().modPow(key, diffieHellmanKey.getP()).toString(16);

        PowerMockito.spy(DiffieHellmanUtil.class);
        PowerMockito.doReturn(key).when(DiffieHellmanUtil.class, "generateRandomKey", anyInt());

        QueryDiffieHellmanKeyRequest request = new QueryDiffieHellmanKeyRequest();
        request.setP(diffieHellmanKey.getP().toString(16));
        request.setG(diffieHellmanKey.getG().toString(16));
        request.setUuid(uuid);

        CacheOperation<BigInteger> mCacheOperation = CacheOperationFactory.getCacheOperation();
        QueryDiffieHellmanKeyService service = new QueryDiffieHellmanKeyService();
        QueryDiffieHellmanKeyResponse response = service.handle(request);
        Assert.assertEquals(uuid, response.getUuid());
        Assert.assertEquals(value, response.getDiffieHellmanValue());
        Assert.assertEquals(diffieHellmanKey.getP(), mCacheOperation.get(uuid, Constants.SA.SA_MOD));
        Assert.assertEquals(key, mCacheOperation.get(uuid, Constants.SA.SA_KEY));

    }
}