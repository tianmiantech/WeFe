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
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;

/**
 * @author hunter.zhao
 */
public class ModelPredictClient {

    // 私钥
    private static final String customer_privateKey = "***"; // TODO
    // 公钥
    private static final String customer_publicKey = "***"; // TODO
    // 客户code
    private static final String customer_code = "***"; // TODO

    private static final String api = "{{baseUrl}}/api/predict/%s"; // TODO

    private static final String serviceId = "xxxx"; // TODO

    public static void main(String[] args) throws Exception {
        String params = setFederatedPredictBody();
        System.out.println("api = " + api);
        System.out.println("params = " + params);
        System.out.println("result = " + sendPost(api, params));
    }

    protected static String setFederatedPredictBody() throws Exception {
        // params 请求入参
        TreeMap<String, Object> params = new TreeMap<>();
        Map<String, Object> map1 = new HashMap<>();
        // TODO 添加特征值到map
        map1.put("特征1", 0.1223213);

        params.put("featureData", map1);
        params.put("userId", "1"); // TODO 传给协作方查找特征使用

        params.put("serviceId", serviceId);
        params.put("partnerCode", customer_code);
        params.put("requestId", UUID.randomUUID().toString().replace("-", ""));

        /**
         * Prevent map disorder, resulting in signature verification failure
         */
        String data = new JSONObject(params).toJSONString();
        /**
         * sign
         */
        String sign;
        try {
            sign = sign(data, customer_privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JSONObject body = new JSONObject();
        body.put("partnerCode", customer_code);
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
        byte[] priByte = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priByte);
        KeyFactory fac = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
        sigEng.initSign(privateKey);
        sigEng.update(data.getBytes());
        return Base64.getEncoder().encodeToString(sigEng.sign());
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
