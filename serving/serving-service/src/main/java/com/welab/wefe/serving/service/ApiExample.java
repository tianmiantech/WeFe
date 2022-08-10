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
package com.welab.wefe.serving.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hunter.zhao
 */
public class ApiExample {

    private static final String api = "{{baseUrl}}/api/predict/%s";

    public static void main(String[] args) throws Exception {
        System.out.println(setFederatedPredictBody());
    }

    protected static String setFederatedPredictBody() throws Exception {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("特征1", 0.1223213);
        map1.put("特征2", 0.1223213);

        /**
         * params
         * 请求入参
         */
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("serviceId", "%s");
        params.put("userId", "手机号");
        params.put("partnerCode", "分配的客户号");
//        params.put("featureData", map1);

        /**
         * Prevent map disorder, resulting in signature verification failure
         */
        String data = new JSONObject(params).toJSONString();

        /**
         * sign
         */
        String sign;
        try {
            sign = sign(data, "私钥");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        JSONObject body = new JSONObject();
        body.put("partnerCode", "客户号");
        body.put("sign", sign);
        body.put("data", data);

        return body.toJSONString();
    }

    private static final String SIGN_ALGORITHM = "SHA1withRSA";

    /**
     * The private key signature
     */
    public static String sign(String data, String privateKeyStr) throws Exception {
        Signature sigEng = Signature.getInstance(SIGN_ALGORITHM);
        byte[] priByte = new Base64().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priByte);
        KeyFactory fac = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
        sigEng.initSign(privateKey);
        sigEng.update(data.getBytes());
        return Base64.encodeBase64String(sigEng.sign());

    }
}
