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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.TreeMap;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.util.RSAUtil;

//import org.bouncycastle.asn1.gm.GMNamedCurves;
//import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
//import org.bouncycastle.asn1.x9.X9ECParameters;
//import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECParameterSpec;
//import org.bouncycastle.jce.spec.ECPrivateKeySpec;

/**
 * 多方安全统计 客户端 <br>
 * 生成http请求参数，然后通过http请求服务 <br>
 * 编译 `javac -cp mpc-sa-sdk-1.0.0.jar:. SaClient.java` <br>
 * 运行 `java -cp mpc-sa-sdk-1.0.0.jar:. SaClient`
 */
public class SaClient {
    // 私钥
    private static final String customer_privateKey = "***"; // TODO
    // 公钥
    private static final String customer_publicKey = "***"; // TODO
    // 客户code
    private static final String customer_code = "***"; // TODO
    // Serving服务地址
    private static final String serverUrl = "http://xxxxx.com/xxxx/"; // TODO 参考readme.md 的serverUrl
    // Service Api name
    private static final String apiName = "api/*****"; // TODO 参考readme.md 的apiName

    public static void main(String[] args) throws Exception {
        JSONObject queryParams = new JSONObject();
        // TODO
        // 参考readme.md params
        // 例如 [{"描述:":"phone","参数名:":"phone"}]
        // queryParams.put("phone", "188xxxxxxxx");
        queryParams.put("xxxx", "xxxx");
        JSONObject data = new JSONObject();
        data.put("query_params", queryParams);
        String params = request(data.toJSONString());
        System.out.println("多方安全统计参数:\t" + params);
        System.out.println("多方安全统计 url:" + serverUrl + apiName);
        System.out.println("响应结果:" + sendPost(serverUrl + apiName, params));
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
            e.printStackTrace();
        } finally {
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
