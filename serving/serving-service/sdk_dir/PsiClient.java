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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.sdk.Psi;
import com.welab.wefe.mpc.psi.sdk.PsiFactory;
import com.welab.wefe.mpc.psi.sdk.excel.AbstractDataSetReader;
import com.welab.wefe.mpc.psi.sdk.excel.CsvDataSetReader;
import com.welab.wefe.mpc.psi.sdk.excel.ExcelDataSetReader;
import com.welab.wefe.mpc.psi.sdk.model.ConfuseData;

/**
 * 两方交集查询 客户端 <br>
 * 配合 mpc-pir-sdk-1.0.0.jar， bcprov-jdk15on-1.69.jar使用 <br>
 * 编译 `javac -cp mpc-psi-sdk-1.0.0.jar:. PsiClient.java` <br>
 * 运行 `java -cp mpc-psi-sdk-1.0.0.jar:. PsiClient xxxxx.csv`
 */
public class PsiClient {
    // 私钥
    private static final String Customer_privateKey = "xxxx";
    // 公钥
    private static final String Customer_publicKey = "xxxx";
    // 客户code
    private static final String Customer_code = "xxxx";
    // Serving服务地址
    private static final String serverUrl = "http://xxxxx.com/xxxx/"; // TODO 参考readme.md 的serverUrl
    // Service Api name
    private static final String apiName = "api/*****"; // TODO 参考readme.md 的apiName
    // ID生成策略参数
    private static final String params = "[{\"field\":\"xxx\",\"operator\":\"xxx\"}]"; // TODO 参考readme.md 的params

    private static Map<String, String> clientDatasetMap; // key:哈希数据 value:原数据

    public static void main(String[] args) throws Exception {
        init(args);
        long start = System.currentTimeMillis();
        // params
        Psi psi = PsiFactory.generatePsi();
        psi.setClientDatasetMap(clientDatasetMap);
        CommunicationConfig config = new CommunicationConfig();
        config.setSignPrivateKey(Customer_privateKey);// 私钥
        config.setSecretKeyType("rsa");
//        config.setSecretKeyType("sm2");
        config.setCommercialId(Customer_code); // 客户ID
        // 服务地址
        config.setServerUrl(serverUrl);
        config.setApiName(apiName);
        // 是否要返回结果标签
        psi.setNeedReturnFields(true);
        // 如果是续跑 需要带上下面两个参数
        // config.setRequestId("xxx");
        // psi.setContinue(true);
        psi.setUsePirToReturnFields(true);
        psi.setConfuseData(generateConfuseData());
        List<String> result = psi.query(config, new ArrayList<>(clientDatasetMap.keySet()));
        System.out.println("client size = " + clientDatasetMap.size() + ", result size = " + result.size()
                + ", duration = " + (System.currentTimeMillis() - start));
        // config.setRequestId("xxx");
        // psi.returnFields(config); // 主动调用返回标签结果
    }

    /**
     * 生成混淆数据
     */
    private static ConfuseData generateConfuseData() {
        List<String> fieldNames = parseFieldsByParams();
        if (fieldNames == null || fieldNames.isEmpty()) {
            return new ConfuseData();
        }
        int base = 4; // 查一条混几条
        ConfuseData data = new ConfuseData();
        data.setGenerateDataFunc(s -> {
            // 根据s生成混淆数据
            List<String> list = new LinkedList<>();
            if (fieldNames.size() == 1) { // 单值
                data.setJson(false);
                data.setSingleFieldName(fieldNames.get(0)); // 字段名
                // TODO
                for (int i = 0; i < base; i++) {
                    list.add(new Random().nextInt(20000) + 20000 + "");
                }
            } else {// json格式数据
                data.setJson(true);
                data.setMixFieldNames(fieldNames); // 字段名列表
                for (int i = 0; i < base; i++) {
                    JSONObject obj = new JSONObject();
                    // TODO generate json by fieldNames
                    // obj.put("xxx", xxx);
                    list.add(obj.toJSONString());
                }
            }
            return list;
        });
        return data;
    }

    private static List<String> parseFieldsByParams() {
        List<String> fields = new ArrayList<>();
        JSONArray keyCalcRules = JSONArray.parseArray(params);
        int size = keyCalcRules.size();
        for (int i = 0; i < size; i++) {
            JSONObject item = keyCalcRules.getJSONObject(i);
            String[] fieldArr = item.getString("field").split(",");
            fields.addAll(Arrays.asList(fieldArr));
        }
        return fields;
    }

    private static void init(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            File file = new File("data.csv");
            if(file.exists()){
                args = new String[1];
                args[0] = "data.csv";
            }
            else {
                throw new Exception("data.csv not exists");
            }
        }
        initClientDatasetByFile(args[0]);
        System.out.println("get client size = " + clientDatasetMap.size());
        if (clientDatasetMap == null || clientDatasetMap.size() <= 0) {
            throw new Exception("file is empty");
        }
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
        StringBuilder encodeValue = new StringBuilder(""); // 哈希后的数据
        JSONObject originValueJson = new JSONObject(); // 原数据
        String tempField = "";
        for (int i = 0; i < size; i++) {
            JSONObject item = keyCalcRules.getJSONObject(i);
            String operator = item.getString("operator");
            String[] fields = item.getString("field").split(",");
            tempField = fields[0];
            StringBuffer v = new StringBuffer();
            for (String field : fields) {
                v.append(data.get(field));
                originValueJson.put(field, data.get(field));
            }
            if ("md5".equalsIgnoreCase(operator)) {
                encodeValue.append(getMD5String(v.toString()));
            } else if ("sha256".equalsIgnoreCase(operator)) {
                encodeValue.append(getSHA256String(v.toString()));
            } else { // 不作处理
                encodeValue.append(v.toString());
            }
        }
        // 哈希数据，原数据
        if (originValueJson.size() == 1) {
            clientDatasetMap.put(encodeValue.toString(), originValueJson.getString(tempField));
        } else {
            clientDatasetMap.put(encodeValue.toString(), originValueJson.toJSONString());
        }
        return encodeValue.toString();
    }

    /**
     * 利用java原生的类实现MD5加密
     *
     * @return
     */
    public static String getMD5String(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 利用java原生的类实现SHA256加密
     *
     * @return
     */
    public static String getSHA256String(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodeStr;
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

}
