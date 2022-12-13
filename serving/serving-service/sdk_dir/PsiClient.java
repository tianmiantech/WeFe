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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import com.welab.wefe.mpc.config.CommunicationConfig;

//两方交集查询 psi 
//配合 mpc-psi-sdk-1.0.0.jar使用
public class PsiClient {
    // 私钥
    private static final String 测试客户1_privateKey = "***";
    // 公钥
    private static final String 测试客户1_publicKey = "***";
    // 客户code
    private static final String 测试客户1_code = "***";
    // Serving服务地址
    private static final String serverUrl = "https://xxx.xxx.com/xxx/"; // TODO
    // Service Api name
    private static final String apiName = "api/xxx/xxx"; // TODO

    private static List<String> clientDataset;

    /**
     * 初始化客户端数据
     */
    private static void initClientDataset() {
        clientDataset = new ArrayList<>();
        int total = 10000;
        while ((total--) > 0) {
            clientDataset.add(getMD5String("" + total * 10));
        }
    }

    public static void main(String[] args) throws Exception {
        initClientDataset();

        long start = System.currentTimeMillis();
        // params
        Psi psi = PsiFactory.generatePsi("dh"); // dh or ecdh
        CommunicationConfig config = new CommunicationConfig();
        config.setNeedSign(true); // 是否需要签名
        config.setSignPrivateKey(测试客户1_privateKey);// 私钥
        config.setCommercialId(测试客户1_code); // 客户ID
        // 服务地址
        config.setServerUrl(serverUrl);
        config.setApiName(apiName);
        List<String> result = psi.query(config, clientDataset);
        // 如果 clientId = result 说明有结果
        System.out.println(clientDataset.size());
        System.out.println(result.size());
        System.out.println("duration = " + (System.currentTimeMillis() - start));
    }

    public static String getMD5String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSHA256String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
