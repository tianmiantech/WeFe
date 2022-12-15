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

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.sdk.excel.AbstractDataSetReader;
import com.welab.wefe.mpc.psi.sdk.excel.CsvDataSetReader;
import com.welab.wefe.mpc.psi.sdk.excel.ExcelDataSetReader;

//两方交集查询 psi
//配合 mpc-psi-sdk-1.0.0.jar使用
public class PsiClient {
    // 私钥
    private static final String 测试客户1_privateKey = "*****";
    // 公钥
    private static final String 测试客户1_publicKey = "*****";
    // 客户code
    private static final String 测试客户1_code = "******";
    // Serving服务地址
    private static final String serverUrl = "http://xxxx.com/serving-xxxx-xxx/";        // TODO 参考readme.md 的serverUrl
    // Service Api name
    private static final String apiName = "api/user/query";                              // TODO 参考readme.md 的apiName
    private static final String params = "[{\"field\":\"xxxx\",\"operator\":\"xxxx\"}]"; // TODO 参考readme.md 的params

    private static Map<String, String> clientDatasetMap; // key 哈希数据， value 原数据

    public static void main(String[] args) throws Exception {
        init(args);
        long start = System.currentTimeMillis();
        // params
        Psi psi = PsiFactory.generatePsi();
        psi.setClientDatasetMap(clientDatasetMap);
        CommunicationConfig config = new CommunicationConfig();
        config.setNeedSign(true); // 是否需要签名
        config.setSignPrivateKey(测试客户1_privateKey);// 私钥
        config.setCommercialId(测试客户1_code); // 客户ID
        // 服务地址
        config.setServerUrl(serverUrl);
        config.setApiName(apiName);
        // 如果是续跑 需要带上下面两个参数
        // config.setRequestId("xxx");
        // config.setContinue(true);
        List<String> result = psi.query(config, new ArrayList<>(clientDatasetMap.keySet()));
        System.out.println("client size = " + clientDatasetMap.size() + ", result size = " + result.size()
                + ", duration = " + (System.currentTimeMillis() - start));
    }

    private static void init(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new Exception("file is empty");
        }
        initClientDatasetByFile(args[0]);
        System.out.println("get client size = " + clientDatasetMap.size());
    }

    private static void initClientDatasetByFile(String file) throws Exception {
        if (file == null || file.trim().equalsIgnoreCase("")) {
            throw new Exception("file is empty");
        }
        AbstractDataSetReader reader = file.endsWith(".csv") ? new CsvDataSetReader(new File(file))
                : new ExcelDataSetReader(new File(file));
        reader.getHeader();
        List<Map<String, Object>> rows = new ArrayList<>();
        reader.readAll(s -> {
            rows.add(s);
        });
        System.out.println("first data = " + rows.get(0));
        clientDatasetMap = new LinkedHashMap<>();
        rows.stream().forEach(s -> calcKey(s));
    }

    public static String calcKey(Map<String, Object> data) {
        JSONArray keyCalcRules = JSONArray.parseArray(params);
        int size = keyCalcRules.size();
        StringBuilder encodeValue = new StringBuilder("");
        StringBuilder originalValue = new StringBuilder("");
        for (int i = 0; i < size; i++) {
            JSONObject item = keyCalcRules.getJSONObject(i);
            String operator = item.getString("operator");
            String[] fields = item.getString("field").split(",");
            StringBuffer v = new StringBuffer();
            for (String field : fields) {
                v.append(data.get(field));
            }
            if ("md5".equalsIgnoreCase(operator)) {
                encodeValue.append(getMD5String(v.toString()));
            } else if ("sha256".equalsIgnoreCase(operator)) {
                encodeValue.append(getSHA256String(v.toString()));
            } else { // 不作处理
                encodeValue.append(v.toString());
            }
            originalValue.append(v);
        }
        // 哈希数据，原数据
        clientDatasetMap.put(encodeValue.toString(), originalValue.toString());
        return encodeValue.toString();
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
