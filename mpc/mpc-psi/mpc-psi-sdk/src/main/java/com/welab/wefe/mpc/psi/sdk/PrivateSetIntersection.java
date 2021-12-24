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
import com.welab.wefe.mpc.util.EncryptUtil;

import java.math.BigInteger;
import java.util.*;

/**
 * @Author: eval
 * @Date: 2021-12-23
 **/
public class PrivateSetIntersection {

    public List<String> query(String url, List<String> ids, int keySize) {
        List<String> result = new ArrayList<>();
        DiffieHellmanKey diffieHellmanKey = DiffieHellmanUtil.generateKey(keySize);
        BigInteger key = DiffieHellmanUtil.generateRandomKey(keySize);
        List<String> encryptIds = new ArrayList<>(ids.size());
        for (String id : ids) {
            encryptIds.add(DiffieHellmanUtil.encrypt(id, key, diffieHellmanKey.getP()).toString(16));
        }
        QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
        request.setP(diffieHellmanKey.getP().toString(16));
        request.setClientIds(encryptIds);
        QueryPrivateSetIntersectionResponse response = PrivateSetIntersectionService.handle(url, request);
        List<String> encryptServerIds = response.getServerEncryptIds();
        List<String> encryptIdWithServerKeys = response.getClientIdByServerKeys();
        Set<Integer> intersectionIndex = new HashSet<>();
        for (String serverId : encryptServerIds) {
            String encryptValue = DiffieHellmanUtil.encrypt(serverId, key, diffieHellmanKey.getP(), false).toString(16);
            int index = encryptIdWithServerKeys.indexOf(encryptValue);
            if (index >= 0) {
                intersectionIndex.add(index);
            }
        }

        intersectionIndex.stream().forEach(i -> result.add(ids.get(i)));

        return result;
    }
}
