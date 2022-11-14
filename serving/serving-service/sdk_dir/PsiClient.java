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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.sdk.PrivateSetIntersection;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

// 两方交集查询 psi 
// 配合 mpc-psi-sdk-1.0.0.jar使用
public class PsiClient {
    // 私钥
    private static final String 测试客户1_privateKey = "***"; // TODO
    // 公钥
    private static final String 测试客户1_publicKey = "***"; // TODO
    // 客户code
    private static final String 测试客户1_code = "TEST***25"; // TODO
    // Serving服务地址
    private static final String serverUrl = "https://****/serving-service-01/"; // TODO
    // Service Api name
    private static final String apiName = "api/*****"; // TODO

    public static void main(String[] args) throws Exception {
        // params
        JSONObject data = new JSONObject();
        data.put("phone_number", "******");
        String clientId = getMD5String(data.getString("phone_number"));
        List<String> clientIds = Arrays.asList(new String[] { clientId });

        PrivateSetIntersection privateSetIntersection = new PrivateSetIntersection();
        CommunicationConfig config = new CommunicationConfig();
        config.setNeedSign(true); // 是否需要签名
        config.setSignPrivateKey(测试客户1_privateKey);// 私钥
        config.setCommercialId(测试客户1_code); // 客户ID
        // 服务地址
        config.setServerUrl(serverUrl);
        config.setApiName(apiName);

        List<String> result = privateSetIntersection.query(config, clientIds, 1024);

        // 如果 clientId = result 说明有结果
        System.out.println(clientId);
        System.out.println(result);
    }

    public static String getMD5String(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSHA256String(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
