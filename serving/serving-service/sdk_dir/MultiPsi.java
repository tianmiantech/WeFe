package com.welab.wefe.mpc;/*
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.TreeMap;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.util.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

//import org.bouncycastle.asn1.gm.GMNamedCurves;
//import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
//import org.bouncycastle.asn1.x9.X9ECParameters;
//import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECParameterSpec;
//import org.bouncycastle.jce.spec.ECPrivateKeySpec;

// 多方交集查询 用来生成http请求参数，然后自己通过http请求
// 如果公私钥是sm2,则依赖 bcprov-jdk15on 1.69
public class MultiPsi {
    // 私钥
    private static final String customer_privateKey = "***"; // TODO
    // 公钥
    private static final String customer_publicKey = "***"; // TODO
    // 客户code
    private static final String customer_code = "***"; // TODO
    // Serving服务地址
    private static final String serverUrl = "http://****/***/"; // TODO
    // Service Api name
    private static final String apiName = "api/*****"; // TODO

    public static void main(String[] args) throws Exception {
        String dataStr = "{\n" + "        \"client_ids\": [\n" + "            \"****\"\n"
                + "        ]\n" + "    }";
        String params = request(dataStr);
        System.out.println("多方交集查询参数:\t" + params);
        // 服务地址
        System.out.println("url: " + serverUrl + apiName);
        System.out.println("响应结果：" + sendPost( serverUrl + apiName, params));
    }

    protected static String request(String dataStr) throws Exception {
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("data", JSONObject.parseObject(dataStr));
        String data = params.get("data").toString();
        String sign = "";
        try {
            sign = signRsa(data, customer_privateKey); // signSm2
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject body = new JSONObject();
        body.put("customer_id", customer_code);
        body.put("sign", sign);
        body.put("data", JSONObject.parseObject(data));
        body.put("requestId", "xxx");
        return body.toJSONString();
    }

    /**
     * The sm2 private key signature
     *
     * 依赖 bcprov-jdk15on 1.69
     */
//    public static String signSm2(String data, String privateKeyStr) throws Exception {
//        Signature signature = Signature.getInstance(
//                GMObjectIdentifiers.sm2sign_with_sm3.toString(), new BouncyCastleProvider());
//        // 将私钥HEX字符串转换为X值
//        BigInteger bigInteger = new BigInteger(privateKeyStr, 16);
//        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
//        X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
//        ECParameterSpec ecParameterSpec = new ECParameterSpec(parameters.getCurve(),
//                parameters.getG(), parameters.getN(), parameters.getH());
//        BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(bigInteger,
//                ecParameterSpec));
//        signature.initSign(privateKey);
//        signature.update(data.getBytes(StandardCharsets.UTF_8));
//        return Base64.getEncoder().encodeToString(signature.sign());
//    }

    /**
     * The rsa private key signature
     */
    public static String signRsa(String data, String privateKeyStr) throws Exception {
        Signature sigEng = Signature.getInstance("SHA1withRSA");
        byte[] priByte = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priByte);
        KeyFactory fac = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
        sigEng.initSign(privateKey);
        sigEng.update(data.getBytes());
        return Base64.getEncoder().encodeToString(sigEng.sign());
    }

    /**
     * 将byte转为16进制
     *
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }


    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
