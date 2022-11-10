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
import com.welab.wefe.mpc.util.RSAUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.TreeMap;

// 多方安全统计 用来生成http请求参数，然后自己通过http请求
public class SaClient {
    // 私钥
    private static final String 测试客户1_privateKey = "***"; // TODO
    // 公钥
    private static final String 测试客户1_publicKey = "***"; // TODO
    // 客户code
    private static final String 测试客户1_code = "TEST***25"; // TODO
    // Serving服务地址
    private static final String serverUrl = "https://****/serving-service-01/"; // TODO
    // Service Api name
    private String apiName = "api/*****"; // TODO

    public static void main(String[] args) throws Exception {
        // params
        String dataStr = "{\n" + "  \"query_params\": {\n" + "    \"mobile\": \"*******\"\n" + "  }\n" + "}";
        System.out.println("多方安全统计参数:\t" + request(dataStr));
        // 服务地址
        System.out.println("多方安全统计 url:" + serverUrl + apiName);
        System.out.println("返回结果:" + sendPost(serverUrl + apiName, request(dataStr)));
    }

    protected static String request(String dataStr) throws Exception {
        TreeMap<String, Object> params = new TreeMap<>();
        params.put("data", JSONObject.parseObject(dataStr));
        String data = params.get("data").toString();
        String sign = "";
        try {
            sign = RSAUtil.sign(data, 测试客户1_privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject body = new JSONObject();
        body.put("customer_id", 测试客户1_code);
        body.put("sign", sign);
        body.put("data", JSONObject.parseObject(data));
        body.put("requestId", "xxx");
        boolean verified = RSAUtil.verify(params.get("data").toString().getBytes(),
                RSAUtil.getPublicKey(测试客户1_publicKey), sign);
        if (verified) {
            return body.toJSONString();
        }
        return "";
    }

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
