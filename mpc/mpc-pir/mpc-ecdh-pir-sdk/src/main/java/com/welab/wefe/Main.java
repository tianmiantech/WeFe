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

package com.welab.wefe;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.welab.wefe.entity.EcdhPsiClient;
import com.welab.wefe.entity.EcdhPsiServer;

public class Main {

    private static Set<String> serverDataset;
    private static Set<String> clientDataset;

    public static void main(String[] args) {
        long serverSize = 30;
        long clientSize = 20;
        long intersectionSize = 3;
        initDatasets(serverSize, clientSize, intersectionSize);

        EcdhPsiClient client = new EcdhPsiClient();
        EcdhPsiServer server = new EcdhPsiServer();

        // 客户端加载并加密客户端数据集，map中的key为临时生成的自增序列ID
        Map<Long, String> clientEncryptedDatasetMap = client.encryptClientOriginalDataset(clientDataset);
        // 客户端发给服务端，服务端进行二次加密
        Map<Long, String> doubleEncryptedClientDatasetMap = server.encryptDatasetMap(clientEncryptedDatasetMap);
        // 服务端将二次加密后的数据发给客户端，客户端进行转换成椭圆曲线上的点
        client.convertDoubleEncryptedClientDataset2ECPoint(doubleEncryptedClientDatasetMap);
        // 服务端对自己的数据集进行加密
        Set<String> serverEncryptedDataset = server.encryptDataset(serverDataset);
        // 服务端发给客户端,客户端进行二次加密
        client.encryptServerDataset(serverEncryptedDataset);
        // 进行对齐
        Set<String> psiResult = client.psi();
        psiResult.stream().forEach(System.out::println);
    }

    private static void initDatasets(long serverSize, long clientSize, long intersectionSize) {
        initServerDataset(intersectionSize, serverSize - intersectionSize);
        initClientDataset(intersectionSize, clientSize - intersectionSize);
    }

    private static void initClientDataset(long matching, long mismatching) {
        clientDataset = new HashSet<>();
        for (long i = 0; i < matching; i++)
            clientDataset.add("MATCHING-" + i);
        for (long i = matching; i < (matching + mismatching); i++)
            clientDataset.add("CLIENT-ONLY-" + i);
    }

    private static void initServerDataset(long matching, long mismatching) {
        serverDataset = new HashSet<>();
        for (long i = 0; i < matching; i++)
            serverDataset.add("MATCHING-" + i);
        for (long i = 0; i < mismatching; i++)
            serverDataset.add("SERVER-ONLY-" + i);
    }
}