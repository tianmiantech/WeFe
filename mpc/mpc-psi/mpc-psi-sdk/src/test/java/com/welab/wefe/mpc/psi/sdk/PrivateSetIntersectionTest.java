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

package com.welab.wefe.mpc.psi.sdk;

import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.service.PrivateSetIntersectionService;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.mockito.Matchers.*;

/**
 * @Author: eval
 * @Date: 2021-12-24
 **/
@RunWith(PowerMockRunner.class)
@PrepareForTest({
        PrivateSetIntersectionService.class,
        DiffieHellmanUtil.class
})
public class PrivateSetIntersectionTest {

    @Before
    public void prepare() {
        MockitoAnnotations.initMocks(this);
    }

    private QueryPrivateSetIntersectionRequest generateRequest(DiffieHellmanKey diffieHellmanKey, List<String> ids, BigInteger clientKey) {
        QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
        request.setP(diffieHellmanKey.getP().toString(16));
        List<String> values = new ArrayList<>(ids.size());
        ids.forEach(id ->
                values.add(DiffieHellmanUtil.encrypt(id, clientKey, diffieHellmanKey.getP()).toString(16))
        );
        request.setClientIds(values);
        return request;
    }

    private QueryPrivateSetIntersectionResponse generateResponse(QueryPrivateSetIntersectionRequest request, List<String> serverIds, BigInteger serverKey) {
        QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
        BigInteger mod = new BigInteger(request.getP(), 16);

        List<String> encryptServerIds = new ArrayList<>(serverIds.size());
        serverIds.forEach(serverId -> encryptServerIds.add(DiffieHellmanUtil.encrypt(serverId, serverKey, mod).toString(16)));
        response.setServerEncryptIds(encryptServerIds);

        List<String> encryptClientIds = new ArrayList<>(request.getClientIds().size());
        request.getClientIds().forEach(
                id -> encryptClientIds.add(DiffieHellmanUtil.encrypt(id, serverKey, mod, false).toString(16))
        );
        response.setClientIdByServerKeys(encryptClientIds);
        return response;
    }

    @Test
    public void testQuery() {
        int keySize = 1024;
        int clientSeed = 33333;
        int serverSeed = 44444;
        String url = "http://127.0.0.1:8080/psi";
        List<String> ids = Arrays.asList(new String[]{
                "13012341234", "13012341235", "13012341236"
        });
        List<String> serverIds = Arrays.asList(new String[]{
                "13012341234", "13112341235", "13012341236", "13012341296", "13012341298"
        });

        DiffieHellmanKey diffieHellmanKey = new DiffieHellmanKey();
        diffieHellmanKey.setG(new BigInteger("2", 16));
        diffieHellmanKey.setP(new BigInteger("ffffffffffffffffc90fdaa22168c234c4c6628b80dc1cd129024e088a67cc74020bbea63b139b22514a08798e3404ddef9519b3cd3a431b302b0a6df25f14374fe1356d6d51c245e485b576625e7ec6f44c42e9a637ed6b0bff5cb6f406b7edee386bfb5a899fa5ae9f24117c4b1fe649286651ece65381ffffffffffffffff", 16));
        BigInteger clientKey = new BigInteger(keySize, new Random(clientSeed));
        BigInteger serverKey = new BigInteger(keySize, new Random(serverSeed));

        QueryPrivateSetIntersectionRequest request = generateRequest(diffieHellmanKey, ids, clientKey);
        QueryPrivateSetIntersectionResponse response = generateResponse(request, serverIds, serverKey);

        PowerMockito.mockStatic(PrivateSetIntersectionService.class);
        MockitoAnnotations.initMocks(this);
        Mockito.when(PrivateSetIntersectionService.handle(anyString(), anyObject())).thenReturn(response);

        PowerMockito.mockStatic(DiffieHellmanUtil.class);
        Mockito.when(DiffieHellmanUtil.generateKey(keySize)).thenReturn(diffieHellmanKey);
        Mockito.when(DiffieHellmanUtil.generateRandomKey(keySize)).thenReturn(clientKey);
        Mockito.when(DiffieHellmanUtil.encrypt(anyString(), anyObject(), anyObject(), anyBoolean())).thenCallRealMethod();
        Mockito.when(DiffieHellmanUtil.encrypt(anyString(), anyObject(), anyObject())).thenCallRealMethod();

        PrivateSetIntersection privateSetIntersection = new PrivateSetIntersection();
        List<String> result = privateSetIntersection.query(url, ids, keySize);

        Assert.assertArrayEquals(ids.stream().filter(id -> serverIds.contains(id)).toArray(), result.toArray());
    }
}